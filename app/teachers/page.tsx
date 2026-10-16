import Link from "next/link";
import { teachers } from "@/lib/data";

export default function TeachersPage() {
  return (
    <main>
      <header className="topbar">
        <Link className="brand" href="/">برنامه اساتید</Link>
        <nav className="nav"><Link href="/">خانه</Link></nav>
      </header>

      <section className="hero">
        <span className="eyebrow">اساتید</span>
        <h1>فهرست اساتید</h1>
        <p>یک استاد را انتخاب کنید تا برنامه هفتگی و زمان آخرین دریافت برنامه را ببینید.</p>
      </section>

      <section className="panel">
        <div className="teacher-list">
          {teachers.map((teacher) => (
            <Link className="teacher-row" href={`/teachers/${teacher.id}`} key={teacher.id}>
              <span>
                <strong>{teacher.name}</strong>
                <small>{teacher.faculty}</small>
              </span>
              <span className="arrow" aria-hidden="true">←</span>
            </Link>
          ))}
        </div>
      </section>
    </main>
  );
}
