import {Component} from "@angular/core";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

@Component({
  selector: "app-splash-screen",
  imports: [MatProgressSpinnerModule, MatInputModule, MatProgressBarModule],
  templateUrl: "splash-screen.component.html",
  styleUrls: ["splash-screen.component.scss"],
})
export class SplashScreenComponent {
}
