import {Component, inject, input, InputSignal,} from "@angular/core";
import {MatMenuItem} from "@angular/material/menu";
import {Store} from "@ngrx/store";
import {FyCurrencyPipe, FyDatePipe, SecurityNamePipe,} from "../../../../common";
import {FyIconComponent} from "../../../../common/components/fy-icon/fy-icon.component";
import {SecurityLogoUrlPipe} from "../../../../common/pipe/security-logo-url.pipe";
import {DividendAnnouncementRead} from "../../../../gen/api/notification/dividend-announcement";
import {DividendAnnouncementActions} from "../../../../store/dividend-announcement/dividend-announcement.actions";
import {AcknowledgeButtonComponent} from "../acknowledge-button/acknowledge-button.component";
import {AppState} from "../../../../store/app.state";

@Component({
  selector: "app-dividend-announcement-notification",
  imports: [
    SecurityNamePipe,
    MatMenuItem,
    FyIconComponent,
    SecurityLogoUrlPipe,
    FyDatePipe,
    FyCurrencyPipe,
    AcknowledgeButtonComponent,
    SecurityNamePipe,
  ],
  templateUrl: "./dividend-announcement-notification.component.html",
  styleUrl: "./dividend-announcement-notification.component.scss",
})
export class DividendAnnouncementNotificationComponent {
  dividendAnnouncement: InputSignal<DividendAnnouncementRead> =
    input.required<DividendAnnouncementRead>();
  private readonly dividendAnnouncementStore: Store<AppState> = inject(Store);

  protected acknowledge(event: Event): void {
    event.stopPropagation();
    this.dividendAnnouncementStore.dispatch(
      DividendAnnouncementActions.markAsRead({
        id: this.dividendAnnouncement().id,
      }),
    );
  }
}
