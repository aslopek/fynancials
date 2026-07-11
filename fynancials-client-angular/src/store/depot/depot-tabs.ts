export const tabIndexPositions = 0;
export const tabIndexDividends = 1;
export const tabIndexPerformance = 2;
export const tabIndexTransactions = 3;

export type DepotTab = {
  index: 0
  tab: 'positions'
} | {
  index: 1
  tab: 'dividends'
} | {
  index: 2
  tab: 'performance'
} | {
  index: 3
  tab: 'transactions'
};
