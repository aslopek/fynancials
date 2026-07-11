export function toDateString(date: Date): string {
  const year: number = date.getFullYear();
  const month: number = date.getMonth() + 1;
  const day: number = date.getDate();
  return `${year}-${month < 10 ? '0' : ''}${month}-${day < 10 ? '0' : ''}${day}`;
}