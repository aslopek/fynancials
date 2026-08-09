import {AfterViewInit, Component, ElementRef, inject, Signal, viewChild} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {ReadableStartupStore, StartupStore} from "../startup/store/startup.store";
import {ReadableUnlockStore, UnlockStore} from "./store/unlock.store";

@Component({
  selector: "app-unlock",
  imports: [MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  providers: [UnlockStore],
  templateUrl: "unlock.component.html",
  styleUrls: ["unlock.component.scss"],
})
export class UnlockComponent implements AfterViewInit {

  protected readonly unlockStore: ReadableUnlockStore = inject(UnlockStore);
  protected readonly startupStore: ReadableStartupStore = inject(StartupStore);

  private readonly passwordInput: Signal<ElementRef<HTMLInputElement>> =
    viewChild.required<ElementRef<HTMLInputElement>>("passwordInput");

  ngAfterViewInit(): void {
    this.passwordInput().nativeElement.focus();
  }
}
