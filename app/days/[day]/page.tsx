import Link from "next/link";
import { notFound } from "next/navigation";
import { days, scheduleEntries, teachers } from "@/lib/data";

type Props = {
  params: Promise<{ day: string }>;
};

export default async function DayPage({ params }: Props) {
  const { day: dayKey } = await params;
  const day = days.find((item) => item.key === dayKey);

  if (!day) notFound();

  const entries = scheduleEntries.filter((entry) => entry.day === day.key);

  return (
    <main>
      <Link className="back" href="/">← بازگشت به روزهای هفته</Link>

      <section className="profile">
        <span className="eyebrow">برنامه روزانه</span>
        <h1>{day.label}</h1>
        <p className="meta">برنامه‌های ثبت‌شده برای این روز</p>
      </section>

      <section className="panel schedule">
        <table className="schedule-table">
          <thead>
            <tr>
              <th>استاد</th>
              <th>درس</th>
              <th>ساعت</th>
              <th>کلاس</th>
              <th>مکان</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => {
              const teacher = teachers.find((item) => item.id === entry.teacherId);

              return (
                <tr key={entry.id}>
                  <td>
                    <Link href={`/teachers/${entry.teacherId}`}>
                      {teacher?.name ?? "نامشخص"}
                    </Link>
                  </td>
                  <td>{entry.course}</td>
                  <td>{entry.startTime} تا {entry.endTime}</td>
                  <td>{entry.classNo}</td>
                  <td>{entry.location}</td>
                </tr>
              );
            })}
          </tbody>
        </table>

        {entries.length === 0 && (
          <p className="empty-state">برای این روز برنامه‌ای ثبت نشده است.</p>
        )}
      </section>
    </main>
  );
}
