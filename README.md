# Quy trình Git cho dự án Mobile_AppTheMuse
1. Làm việc code trên nhánh cá nhân: Tạo và chuyển sang nhánh của bạn: git checkout -b feature/ten-cua-ban
2.  Code xong thì commit: git add . và git commit -m "Tính năng vừa làm" Cập nhật code mới nhất từ sub_main (Tránh xung đột)
3.  Trước khi đẩy code lên, hãy kéo code mới nhất từ sub_main về nhánh cá nhân của bạn để giải quyết conflict (nếu có) dưới local
4.  Đẩy code lên và tạo Pull Request (PR) vào sub_main:
5.  ⚠️ Lưu ý quan trọng cho cả team TUYỆT ĐỐI KHÔNG chọn nhầm nhánh đích là main khi tạo PR hoặc khi dùng lệnh git merge.

# MÔ HÌNH KIẾN TRÚC HỆ THỐNG "THE MUSE" (CLEAN ARCHITECTURE)
```text
│app/src/main/java/com/example/appthemuse/
├── 📂 data/                // Nơi lấy dữ liệu (Firebase, Room)
│   ├── 📂 local/           // Room Database (dao, entity, database)
│   ├── 📂 remote/          // Firebase Services (AuthService, FirestoreService, StorageService)
│   └── 📂 repository/      // Các class Impl (thực thi logic từ Domain)
│
├── 📂 domain/              // Nơi chứa luật (Interface, Data Model)
│   ├── 📂 model/           // Các class data chuẩn: Book, User, Category
│   └── 📂 repository/      // Chỉ chứa các Interface (không code xử lý)
│
├── 📂 ui/                  // Nơi hiển thị giao diện
│   ├── 📂 mapper/          // Nơi đặt file Mappers.kt (chuyển Domain -> UI)
│   ├── 📂 model/           // BookUi, CategoryUi (chỉ dùng ở UI)
│   ├── 📂 viewmodel/       // Gọi Repository, dùng Mapper, quản lý State
│   ├── 📂 screens/         // Giao diện (auth, user, admin)
│   ├── 📂 components/      // Nút, thẻ truyện, ô nhập liệu...
│   └── 📂 theme/           // Color, Type, Theme
│
└── 📂 utils/               // Các hàm tiện ích dùng chung
    ├── 📂 extensions/      // Hàm mở rộng (ví dụ: format date)
    └── 📂 constants/       // Các hằng số (Firebase path, key...)

