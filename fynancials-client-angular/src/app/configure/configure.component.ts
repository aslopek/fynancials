import {Component, inject} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FileDirectoryPipe} from "../../common/pipe/file-directory.pipe";
import {FileNamePipe} from "../../common/pipe/file-name.pipe";
import {ReadableStartupStore, StartupStore} from "../startup/store/startup.store";
import {DatabaseSectionComponent} from "./database-section/database-section.component";
import {ConfigureStore, ReadableConfigureStore} from "./store/configure.store";

@Component({
  selector: "app-configure",
  imports: [FileDirectoryPipe, FileNamePipe, DatabaseSectionComponent, MatButtonModule, MatIconModule],
  providers: [ConfigureStore],
  templateUrl: "configure.component.html",
  styleUrls: ["configure.component.scss"],
})
export class ConfigureComponent {

  protected readonly configureStore: ReadableConfigureStore = inject(ConfigureStore);
  protected readonly startupStore: ReadableStartupStore = inject(StartupStore);
}
