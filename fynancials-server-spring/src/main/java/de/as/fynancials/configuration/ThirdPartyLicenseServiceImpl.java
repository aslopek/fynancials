package de.as.fynancials.configuration;

import static java.nio.charset.StandardCharsets.UTF_8;

import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.config.api.model.ThirdPartyLicenseDto;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ThirdPartyLicenseServiceImpl implements ThirdPartyLicenseService {

  private static final String NESTED_LIBRARY_PREFIX = "BOOT-INF/lib/";
  private static final String OWN_GROUP_ID = "de.as.fynancials";
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(.+?)-(\\d[^/]*?)\\.jar$");
  private static final Pattern POM_LICENSE_PATTERN =
      Pattern.compile("<licenses>.*?<license>.*?<name>\\s*(.*?)\\s*</name>", Pattern.DOTALL);
  private static final Pattern LICENSE_FILE_PATTERN =
      Pattern.compile("^(META-INF/)?LICEN[CS]E(\\.(txt|md))?$", Pattern.CASE_INSENSITIVE);
  private static final Pattern NOTICE_FILE_PATTERN =
      Pattern.compile("^(META-INF/)?NOTICE(\\.(txt|md))?$", Pattern.CASE_INSENSITIVE);

  /**
   * One canonical fallback text per SPDX id, used whenever a library's jar doesn't embed its own LICENSE file. Text
   * files live in {@code src/main/resources/licenses/}.
   */
  private static final Map<String, LicenseBucket> LICENSE_BUCKETS_BY_SPDX_ID = Map.of(
      "Apache-2.0", new LicenseBucket("Apache License 2.0", "Apache-2.0.txt"),
      "BSD-3-Clause", new LicenseBucket("BSD 3-Clause License", "BSD-3-Clause.txt"),
      "EPL-1.0", new LicenseBucket("Eclipse Public License 1.0", "EPL-1.0.txt"),
      "EPL-2.0", new LicenseBucket("Eclipse Public License 2.0", "EPL-2.0.txt"),
      "EDL-1.0", new LicenseBucket("Eclipse Distribution License 1.0", "EDL-1.0.txt"),
      "FSL-1.1-ALv2", new LicenseBucket("Functional Source License 1.1 (ALv2 Future License)", "FSL-1.1-ALv2.txt"),
      "aspect-j-weaver", new LicenseBucket("Eclipse Public License 2.0 AND BSD 3-Clause License AND Apache License 1.1",
          "LICENSE-ASPECT-J-WEAVER.txt")
  );

  /**
   * Only for libraries whose jar provides no usable license signal at all, or one too obscure for
   * {@link LibraryInfo#spdxIdFromRawLicense} to recognize generically. Everything else resolves its SPDX id straight from the
   * jar's own manifest/pom, so a future dependency under one of the SPDX ids above needs no code change here.
   */
  private static final Map<String, String> SPDX_ID_OVERRIDE_BY_ARTIFACT = Map.of(
      "antlr4-runtime", "BSD-3-Clause",
      "aspectjweaver", "aspect-j-weaver",
      "h2", "EPL-1.0",
      "json-path", "Apache-2.0"
  );

  private static final Map<String, String> FALLBACK_TEXT_CACHE = new ConcurrentHashMap<>();

  private record LicenseBucket(String displayName, String resourceFileName) {
  }

  private static String readFallbackLicenseText(String resourceFileName) {
    return FALLBACK_TEXT_CACHE.computeIfAbsent(resourceFileName, fileName -> {
      try (InputStream inputStream =
               ThirdPartyLicenseServiceImpl.class.getClassLoader().getResourceAsStream("licenses/" + fileName)) {
        if (inputStream == null) {
          throw new IllegalStateException("Missing bundled license resource: licenses/" + fileName);
        }
        return new String(inputStream.readAllBytes(), UTF_8);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private List<ThirdPartyLicenseDto> cachedLicenses;

  @Override
  public synchronized List<ThirdPartyLicenseDto> getThirdPartyLicenses() throws InternalServerErrorException {
    if (cachedLicenses == null) {
      cachedLicenses = collectLicenses();
    }
    return cachedLicenses;
  }

  private List<ThirdPartyLicenseDto> collectLicenses() throws InternalServerErrorException {
    List<ThirdPartyLicenseDto> licenses = new ArrayList<>();

    for (String classPathEntry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
      if (!classPathEntry.endsWith(".jar")) {
        continue;
      }

      try (JarFile jarFile = new JarFile(classPathEntry)) {
        if (jarFile.getEntry(NESTED_LIBRARY_PREFIX) != null) {
          collectFromFatJar(jarFile, licenses);
        } else {
          collectFromLibraryJar(jarFile, classPathEntry, licenses);
        }
      } catch (IOException e) {
        log.warn("Could not read classpath entry {} while collecting third-party licenses: {}", classPathEntry, e.getMessage());
      }
    }

    if (licenses.isEmpty()) {
      log.error("No third-party licenses could be collected from the classpath");
      throw new InternalServerErrorException();
    }

    licenses.sort(Comparator.comparing(ThirdPartyLicenseDto::getName, String.CASE_INSENSITIVE_ORDER));
    return licenses;
  }

  private void collectFromFatJar(JarFile fatJar, List<ThirdPartyLicenseDto> licenses) {
    fatJar.stream().filter(entry -> entry.getName().startsWith(NESTED_LIBRARY_PREFIX) && entry.getName().endsWith(".jar"))
        .forEach(entry -> {
          try (JarInputStream nestedJar = new JarInputStream(fatJar.getInputStream(entry))) {
            String fileName = entry.getName().substring(NESTED_LIBRARY_PREFIX.length());
            addIfThirdParty(readNestedJar(nestedJar, fileName), licenses);
          } catch (IOException e) {
            log.warn("Could not read nested library {} while collecting third-party licenses: {}", entry.getName(), e.getMessage());
          }
        });
  }

  private void collectFromLibraryJar(JarFile jarFile, String path, List<ThirdPartyLicenseDto> licenses) throws IOException {
    LibraryInfo library = new LibraryInfo(new File(path).getName());
    library.applyManifest(jarFile.getManifest());

    for (JarEntry entry : jarFile.stream().filter(e -> LibraryInfo.isRelevantEntry(e.getName())).toList()) {
      try (InputStream inputStream = jarFile.getInputStream(entry)) {
        library.applyEntry(entry, inputStream);
      }
    }
    addIfThirdParty(library, licenses);
  }

  private LibraryInfo readNestedJar(JarInputStream nestedJar, String fileName) throws IOException {
    LibraryInfo library = new LibraryInfo(fileName);
    library.applyManifest(nestedJar.getManifest());

    ZipEntry entry;
    while ((entry = nestedJar.getNextEntry()) != null) {
      library.applyEntry(entry, nestedJar);
    }
    return library;
  }

  private void addIfThirdParty(LibraryInfo library, List<ThirdPartyLicenseDto> licenses) {
    if (OWN_GROUP_ID.equals(library.groupId) || library.name == null || library.name.startsWith("fynancials")) {
      return;
    }
    licenses.add(library.toDto());
  }

  /**
   * Accumulates the license-relevant files of a single library jar while iterating its entries exactly once.
   */
  private static class LibraryInfo {

    private String name;
    private String version;
    private String groupId;
    private String license;
    private String licenseText;
    private String noticeText;

    private LibraryInfo(String fileName) {
      Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
      if (matcher.matches()) {
        name = matcher.group(1);
        version = matcher.group(2);
      } else {
        name = fileName;
      }
    }

    private void applyManifest(Manifest manifest) {
      if (manifest == null) {
        return;
      }
      Attributes attributes = manifest.getMainAttributes();
      if (license == null) {
        license = attributes.getValue("Bundle-License");
      }
      if (version == null) {
        version = attributes.getValue("Implementation-Version");
      }
    }

    private static boolean isRelevantEntry(String entryName) {
      return LICENSE_FILE_PATTERN.matcher(entryName).matches()
          || NOTICE_FILE_PATTERN.matcher(entryName).matches()
          || (entryName.startsWith("META-INF/maven/") && (entryName.endsWith("/pom.properties") || entryName.endsWith("/pom.xml")));
    }

    private void applyEntry(ZipEntry entry, InputStream content) throws IOException {
      String entryName = entry.getName();

      if (LICENSE_FILE_PATTERN.matcher(entryName).matches() && licenseText == null) {
        licenseText = new String(content.readAllBytes(), UTF_8);
      } else if (NOTICE_FILE_PATTERN.matcher(entryName).matches() && noticeText == null) {
        noticeText = new String(content.readAllBytes(), UTF_8);
      } else if (entryName.startsWith("META-INF/maven/") && entryName.endsWith("/pom.properties")) {
        Properties pomProperties = new Properties();
        pomProperties.load(content);
        name = pomProperties.getProperty("artifactId", name);
        version = pomProperties.getProperty("version", version);
        groupId = pomProperties.getProperty("groupId", groupId);
      } else if (entryName.startsWith("META-INF/maven/") && entryName.endsWith("/pom.xml") && license == null) {
        Matcher matcher = POM_LICENSE_PATTERN.matcher(new String(content.readAllBytes(), UTF_8));
        if (matcher.find()) {
          license = matcher.group(1);
        }
      }
    }

    private ThirdPartyLicenseDto toDto() {
      ThirdPartyLicenseDto dto = new ThirdPartyLicenseDto();
      dto.setName(name);
      dto.setVersion(version != null ? version : "unknown");

      LicenseBucket fallback = licenseText == null ? resolveFallbackBucket() : null;
      if (fallback != null) {
        dto.setLicense(fallback.displayName());
        dto.setLicenseText(readFallbackLicenseText(fallback.resourceFileName()));
      } else {
        dto.setLicense(license != null ? normalizeLicenseName(license) : licenseNameFromText());
        dto.setLicenseText(licenseText);
      }
      dto.setNoticeText(noticeText);
      return dto;
    }

    /**
     * Only reached when the jar embeds no LICENSE file. Resolves an SPDX id — either straight from the jar's own
     * manifest/pom ({@link #spdxIdFromRawLicense}), or, failing that, from {@link #SPDX_ID_OVERRIDE_BY_ARTIFACT} —
     * and looks up the matching canonical text. Returns {@code null} if neither yields a recognized SPDX id.
     */
    private LicenseBucket resolveFallbackBucket() {
      String spdxId = license != null ? spdxIdFromRawLicense(license) : null;
      if (spdxId == null) {
        spdxId = SPDX_ID_OVERRIDE_BY_ARTIFACT.get(name);
      }
      return spdxId != null ? LICENSE_BUCKETS_BY_SPDX_ID.get(spdxId) : null;
    }

    /**
     * Maps the raw {@code Bundle-License}/pom value (often a bare license URL) to an SPDX id, wherever it's
     * generically recognizable. Returns {@code null} rather than guessing.
     */
    private static String spdxIdFromRawLicense(String rawLicense) {
      String lowerCase = rawLicense.toLowerCase();
      if (lowerCase.contains("apache") && lowerCase.contains("2.0")) {
        return "Apache-2.0";
      }
      if (lowerCase.contains("bsd")) {
        return "BSD-3-Clause";
      }
      if (lowerCase.contains("edl-v10") || lowerCase.contains("eclipse distribution license")) {
        return "EDL-1.0";
      }
      if (lowerCase.contains("epl-2.0") || lowerCase.contains("epl-v20")
          || (lowerCase.contains("eclipse public license") && lowerCase.contains("2.0"))) {
        return "EPL-2.0";
      }
      if (lowerCase.contains("epl-v10") || lowerCase.contains("epl-1.0") || lowerCase.contains("eclipse public license")) {
        return "EPL-1.0";
      }
      if (lowerCase.contains("fsl")) {
        return "FSL-1.1-ALv2";
      }
      return null;
    }

    /**
     * Maps the raw {@code Bundle-License}/pom value to a readable license name, reusing {@link #spdxIdFromRawLicense}
     * where possible. Unknown values are passed through unchanged rather than guessed.
     */
    private static String normalizeLicenseName(String rawLicense) {
      String spdxId = spdxIdFromRawLicense(rawLicense);
      if (spdxId != null) {
        return LICENSE_BUCKETS_BY_SPDX_ID.get(spdxId).displayName();
      }
      String lowerCase = rawLicense.toLowerCase();
      if (lowerCase.contains("mozilla") || lowerCase.contains("mpl")) {
        return "Mozilla Public License 2.0";
      }
      if (lowerCase.contains("lesser general public") || lowerCase.contains("lgpl")) {
        return "GNU Lesser General Public License";
      }
      return rawLicense;
    }

    private String licenseNameFromText() {
      if (licenseText == null) {
        return null;
      }
      if (licenseText.contains("Apache License") && licenseText.contains("Version 2.0")) {
        return "Apache License 2.0";
      }
      if (licenseText.contains("Permission is hereby granted, free of charge")) {
        return "MIT License";
      }
      if (licenseText.contains("Eclipse Public License")) {
        return "Eclipse Public License";
      }
      if (licenseText.contains("Mozilla Public License")) {
        return "Mozilla Public License 2.0";
      }
      if (licenseText.contains("GNU Lesser General Public License")) {
        return "GNU Lesser General Public License";
      }
      if (licenseText.contains("Redistribution and use in source and binary forms")) {
        return "BSD License";
      }
      return null;
    }
  }
}
