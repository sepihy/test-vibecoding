import Link from "next/link";
import { notFound } from "next/navigation";
import { days, getTeacher, getTeacherSchedule } from "@/lib/data";

type Props = {
  params: Promise<{ teacherId: string }>;
};

export default async function SchedulePage({ params }: Props) {
  const { teacherId } = await params;
  const teacher = getTeacher(teacherId);

  if (!teacher) notFound();

  const entries = getTeacherSchedule(teacher.id);

  return (
    <main>
      <Link className="back" href={`/teachers/${teacher.id}`}>← بازگشت به پروفایل استاد</Link>

      <section className="profile">
        <span className="eyebrow">برنامه هفتگی</span>
        <h1>{teacher.name}</h1>
        <p className="meta">آخرین زمان دریافت برنامه: {teacher.latestScheduleReceivedAt}</p>
      </section>

      <section className="panel schedule">
        <table className="schedule-table">
          <thead>
            <tr>
              <th>روز</th>
              <th>درس</th>
              <th>ساعت</th>
              <th>کلاس</th>
              <th>مکان</th>
            </tr>
          </thead>
          <tbody>
            {days.flatMap((day) =>
              entries
                .filter((entry) => entry.day === day.key)
                .map((entry) => (
                  <tr key={entry.id}>
                    <td>{day.label}</td>
                    <td>{entry.course}</td>
                    <td>{entry.startTime} تا {entry.endTime}</td>
                    <td>{entry.classNo}</td>
                    <td>{entry.location}</td>
                  </tr>
                )),
            )}
          </tbody>
        </table>
        {entries.length === 0 && (
          <p className="empty-state">برای این استاد برنامه‌ای ثبت نشده است.</p>
        )}
      </section>
    </main>
  );
}
