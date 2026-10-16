export type DayKey =
  | "saturday"
  | "sunday"
  | "monday"
  | "tuesday"
  | "wednesday"
  | "thursday"
  | "friday";

export type Day = {
  key: DayKey;
  label: string;
};

export type Teacher = {
  id: string;
  name: string;
  faculty: string;
  latestScheduleReceivedAt: string;
};

export type ScheduleEntry = {
  id: string;
  teacherId: string;
  day: DayKey;
  course: string;
  classNo: string;
  startTime: string;
  endTime: string;
  location: string;
};

export const days: Day[] = [
  { key: "saturday", label: "شنبه" },
  { key: "sunday", label: "یکشنبه" },
  { key: "monday", label: "دوشنبه" },
  { key: "tuesday", label: "سه‌شنبه" },
  { key: "wednesday", label: "چهارشنبه" },
  { key: "thursday", label: "پنجشنبه" },
  { key: "friday", label: "جمعه" },
];

export const teachers: Teacher[] = [
  {
    id: "ahmadi",
    name: "دکتر علی احمدی",
    faculty: "حقوق",
    latestScheduleReceivedAt: "۱۴۰۵/۰۷/۲۴ - ۰۹:۳۰",
  },
  {
    id: "karimi",
    name: "دکتر سارا کریمی",
    faculty: "علوم انسانی",
    latestScheduleReceivedAt: "۱۴۰۵/۰۷/۲۳ - ۱۵:۱۰",
  },
  {
    id: "moradi",
    name: "دکتر رضا مرادی",
    faculty: "مدیریت",
    latestScheduleReceivedAt: "۱۴۰۵/۰۷/۲۲ - ۱۱:۴۵",
  },
];

export const scheduleEntries: ScheduleEntry[] = [
  {
    id: "ahmadi-sat-1",
    teacherId: "ahmadi",
    day: "saturday",
    course: "حقوق مدنی ۱",
    classNo: "گروه ۱",
    startTime: "۰۸:۰۰",
    endTime: "۱۰:۰۰",
    location: "کلاس ۱۰۱",
  },
  {
    id: "ahmadi-mon-1",
    teacherId: "ahmadi",
    day: "monday",
    course: "حقوق تجارت",
    classNo: "گروه ۲",
    startTime: "۱۰:۰۰",
    endTime: "۱۲:۰۰",
    location: "کلاس ۲۰۴",
  },
  {
    id: "ahmadi-wed-1",
    teacherId: "ahmadi",
    day: "wednesday",
    course: "آیین دادرسی مدنی",
    classNo: "گروه ۱",
    startTime: "۱۴:۰۰",
    endTime: "۱۶:۰۰",
    location: "کلاس ۳۰۲",
  },
  {
    id: "karimi-sun-1",
    teacherId: "karimi",
    day: "sunday",
    course: "روان‌شناسی اجتماعی",
    classNo: "گروه ۱",
    startTime: "۰۹:۰۰",
    endTime: "۱۱:۰۰",
    location: "کلاس ۱۱۰",
  },
  {
    id: "karimi-thu-1",
    teacherId: "karimi",
    day: "thursday",
    course: "روش تحقیق",
    classNo: "گروه ۲",
    startTime: "۱۲:۰۰",
    endTime: "۱۴:۰۰",
    location: "کلاس ۲۱۵",
  },
  {
    id: "moradi-tue-1",
    teacherId: "moradi",
    day: "tuesday",
    course: "مدیریت منابع انسانی",
    classNo: "گروه ۱",
    startTime: "۰۸:۳۰",
    endTime: "۱۰:۳۰",
    location: "کلاس ۴۰۱",
  },
];

export function getTeacher(id: string) {
  return teachers.find((teacher) => teacher.id === id);
}

export function getTeacherSchedule(teacherId: string) {
  return scheduleEntries.filter((entry) => entry.teacherId === teacherId);
}
