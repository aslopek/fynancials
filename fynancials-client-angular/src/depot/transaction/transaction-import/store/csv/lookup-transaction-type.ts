import {TransactionType} from '../../../../../gen/api/depot-transaction';

export function lookupTransactionType(transactionTypeMapping: { [csvValue: string]: TransactionType | null },
                                      csvValue: string): TransactionType | null | undefined {
  return Object.hasOwn(transactionTypeMapping, csvValue) ? transactionTypeMapping[csvValue] : undefined;
}
