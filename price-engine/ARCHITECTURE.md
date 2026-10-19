# موتور قیمت روز لوازم یدکی

## هدف
تبدیل ورودی آزاد کاربر به یک قطعه استاندارد، جمع‌آوری قیمت از منابع مجاز، تطبیق آگهی‌ها با قطعه، حذف داده‌های پرت، وزن‌دهی بر اساس اعتبار و تازگی، و ارائه قیمت پیشنهادی با سطح اطمینان.

## زنجیره
Input -> Normalize -> Identify Part -> Source Adapters -> Validate -> Deduplicate -> Outlier Filter -> Weighted Market Price -> Selling Price

## منابع
هر منبع یک Adapter مستقل دارد. Adapter فقط از روش دسترسی مجاز همان منبع استفاده می‌کند:
- official manufacturer / ISACO
- public e-commerce/product pages
- public distributor feeds
- user-provided supplier lists
- public messaging content where technically and legally accessible

برای WhatsApp/Rubika/گروه‌های خصوصی، موتور نباید دور زدن احراز هویت، محدودیت API یا دسترسی خصوصی انجام دهد.

## مدل اعتماد
امتیاز قیمت از ترکیب این عوامل ساخته می‌شود:
sourceTrust + freshness + productMatch + sellerConsistency.

قیمت‌های قدیمی، بدون مشخصات، یا با تطبیق ضعیف وزن پایین می‌گیرند.

## قیمت نهایی
میانگین ساده استفاده نمی‌شود. ابتدا قیمت‌های پرت با IQR/MAD حذف می‌شوند؛ سپس weighted median به‌عنوان قیمت بازار استفاده می‌شود.

خروجی همیشه شامل بازه بازار، قیمت مرکزی، تعداد منابع، زمان آخرین بررسی و confidence است.
