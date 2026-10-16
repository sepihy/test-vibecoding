import Link from "next/link";
import { days } from "@/lib/data";

export function DaysGrid() {
  return (
    <div className="days-grid">
      {days.map((day) => (
        <Link className="day-card" href={`/?day=${day.key}`} key={day.key}>
          <span>{day.label}</span>
          <small>برنامه هفتگی</small>
        </Link>
      ))}
    </div>
  );
}
