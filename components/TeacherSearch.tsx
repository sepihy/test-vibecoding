"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { teachers } from "@/lib/data";

export function TeacherSearch() {
  const [query, setQuery] = useState("");

  const results = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase("fa-IR");
    if (!normalized) return teachers;
    return teachers.filter((teacher) =>
      `${teacher.name} ${teacher.faculty}`
        .toLocaleLowerCase("fa-IR")
        .includes(normalized),
    );
  }, [query]);

  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">جست‌وجوی فوری</span>
          <h2>استاد را پیدا کنید</h2>
        </div>
        <span className="result-count">{results.length} نتیجه</span>
      </div>

      <label className="search-box">
        <span className="sr-only">جست‌وجوی استاد</span>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="نام استاد یا دانشکده..."
          type="search"
        />
      </label>

      <div className="teacher-list">
        {results.map((teacher) => (
          <Link className="teacher-row" href={`/teachers/${teacher.id}`} key={teacher.id}>
            <span>
              <strong>{teacher.name}</strong>
              <small>{teacher.faculty}</small>
            </span>
            <span className="arrow" aria-hidden="true">←</span>
          </Link>
        ))}
        {results.length === 0 && (
          <p className="empty-state">استادی با این مشخصات پیدا نشد.</p>
        )}
      </div>
    </section>
  );
}
