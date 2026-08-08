import {Component, inject, Signal} from "@angular/core";
import {ReadableStartupStore, StartupStore} from "../startup/store/startup.store";
import {StartupMode} from "../startup/startup-bridge.type";

/**
 * Story #35 creates this component with only what its own ACs need to be exercised: a heading and a line naming
 * the mode. #37 fills it in with the real database/Java configuration screen (sections, file dialogs, "Save &
 * start" / "Discard & start", "Forget the remembered password").
 */
@Component({
  selector: "app-configure",
  templateUrl: "configure.component.html",
  styleUrls: ["configure.component.scss"],
})
export class ConfigureComponent {

  private readonly startupStore: ReadableStartupStore = inject(StartupStore);
  protected readonly mode: Signal<StartupMode | null> = this.startupStore.mode;
}
