package de.as.traquity.admin;

import de.as.traquity.admin.api.model.DatabaseConfigDto;

public interface AdminService {

  /**
   * Returns how the running backend is connected to its database. The password is only filled while development mode
   * is active, and the file location only for a file-backed H2 database.
   */
  DatabaseConfigDto getDatabaseConfig();

  boolean isDevModeActive();

  void setDevModeActive(boolean active);
}
