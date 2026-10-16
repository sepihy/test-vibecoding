import Link from "next/link";
import { days } from "@/lib/data";

export function DaysGrid() {
  return (
    <div className="days-grid">
      {days.map((day) => (
        <Link className="day-card" href={`/days/${day.key}`} key={day.key}>
          <span>{day.label}</span>
          <small>مشاهده برنامه</small>
        </Link>
      ))}
    </div>
  );
}
