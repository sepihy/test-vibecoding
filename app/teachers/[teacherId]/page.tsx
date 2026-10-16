import Link from "next/link";
import { notFound } from "next/navigation";
import { getTeacher, getTeacherSchedule } from "@/lib/data";

type Props = {
  params: Promise<{ teacherId: string }>;
};

export default async function TeacherPage({ params }: Props) {
  const { teacherId } = await params;
  const teacher = getTeacher(teacherId);

  if (!teacher) notFound();

  const entries = getTeacherSchedule(teacher.id);

  return (
    <main>
      <Link className="back" href="/teachers">← بازگشت به فهرست اساتید</Link>

      <section className="profile">
        <span className="eyebrow">{teacher.faculty}</span>
        <h1>{teacher.name}</h1>
        <p className="meta">آخرین زمان دریافت برنامه: {teacher.latestScheduleReceivedAt}</p>
      </section>

      <section className="panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">برنامه هفتگی</span>
            <h2>کلاس‌های {teacher.name}</h2>
          </div>
          <Link className="badge" href={`/schedule/${teacher.id}`}>نمایش کامل</Link>
        </div>

        {entries.length === 0 ? (
          <p className="empty-state">برای این استاد برنامه‌ای ثبت نشده است.</p>
        ) : (
          <div className="schedule">
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
                {entries.map((entry) => (
                  <tr key={entry.id}>
                    <td>{entry.day}</td>
                    <td>{entry.course}</td>
                    <td>{entry.startTime} تا {entry.endTime}</td>
                    <td>{entry.classNo}</td>
                    <td>{entry.location}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}
