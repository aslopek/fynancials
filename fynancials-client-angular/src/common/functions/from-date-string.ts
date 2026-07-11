export function fromDateString(dateString: string): Date {
  const regex: RegExp = /^\d{4}-\d{2}-\d{2}$/;

  if (!regex.test(dateString)) {
    throw new Error(`${dateString} is not a valid date in format yyyy-MM-dd`);
  }

  const [year, month, day] = dateString.split('-');
  return new Date(parseInt(year, 10), parseInt(month, 10) - 1, parseInt(day, 10));
}