import {Component, inject, signal, WritableSignal} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {ReadableStartupStore, StartupStore} from "../startup/store/startup.store";

/**
 * Story #35 creates this component with only what its own ACs need to be exercised: a password input and an OK
 * button that triggers a start. #36 fills it in with local hash verification against the stored record,
 * OK-enablement, an error surface, Cancel and "Use a different database…" - none of that lives here yet.
 */
@Component({
  selector: "app-unlock",
  imports: [MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: "unlock.component.html",
  styleUrls: ["unlock.component.scss"],
})
export class UnlockComponent {

  protected readonly password: WritableSignal<string> = signal<string>("");
  private readonly startupStore: ReadableStartupStore = inject(StartupStore);

  protected unlock(): void {
    this.startupStore.startBackend(this.password());
  }
}
