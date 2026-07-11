import {Component, input, InputSignal} from "@angular/core";
import {NgClass} from "@angular/common";
import {IconSize} from "./icon-size.type";

@Component({
  selector: "fy-icon",
  imports: [
    NgClass
  ],
  templateUrl: "./fy-icon.component.html",
  styleUrl: "./fy-icon.component.scss",
})
export class FyIconComponent {

  readonly src: InputSignal<string> = input.required<string>();
  readonly size: InputSignal<IconSize | undefined> = input<IconSize | undefined>();
}
