import Link from "next/link";
import { DaysGrid } from "@/components/DaysGrid";
import { TeacherSearch } from "@/components/TeacherSearch";

export default function HomePage() {
  return (
    <main>
      <header className="topbar">
        <Link className="brand" href="/">برنامه اساتید</Link>
        <nav className="nav" aria-label="ناوبری اصلی">
          <Link href="/teachers">اساتید</Link>
        </nav>
      </header>

      <section className="hero">
        <span className="eyebrow">MVP</span>
        <h1>برنامه هفتگی را سریع پیدا کنید.</h1>
        <p>روز هفته را ببینید، استاد را جست‌وجو کنید و برنامه کامل او را باز کنید.</p>
      </section>

      <div className="grid">
        <section className="panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">هفته</span>
              <h2>روزهای هفته</h2>
            </div>
          </div>
          <DaysGrid />
        </section>

        <TeacherSearch />
      </div>
    </main>
  );
}
