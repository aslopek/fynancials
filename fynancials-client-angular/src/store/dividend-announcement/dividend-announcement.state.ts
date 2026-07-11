import {DividendAnnouncementDataSourceRead, DividendAnnouncementRead} from '../../gen/api/notification/dividend-announcement';

export type DividendAnnouncementState = {
  dividendAnnouncements: DividendAnnouncementRead[]
  dataSources: { [id: number]: DividendAnnouncementDataSourceRead }
}