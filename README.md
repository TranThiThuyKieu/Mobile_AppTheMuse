# Quy trình Git cho dự án Mobile_AppTheMuse
1. Làm việc code trên nhánh cá nhân: Tạo và chuyển sang nhánh của bạn: git checkout -b feature/ten-cua-ban
2.  Code xong thì commit: git add . và git commit -m "Tính năng vừa làm" Cập nhật code mới nhất từ sub_main (Tránh xung đột)
3.  Trước khi đẩy code lên, hãy kéo code mới nhất từ sub_main về nhánh cá nhân của bạn để giải quyết conflict (nếu có) dưới local
4.  Đẩy code lên và tạo Pull Request (PR) vào sub_main:
5.  ⚠️ Lưu ý quan trọng cho cả team TUYỆT ĐỐI KHÔNG chọn nhầm nhánh đích là main khi tạo PR hoặc khi dùng lệnh git merge.

# MÔ HÌNH KIẾN TRÚC HỆ THỐNG "THE MUSE" (CLEAN ARCHITECTURE)
```text
app/src/main/java/com/example/appthemuse/
│
├── 📂 data/ (TẦNG DỮ LIỆU - Xử lý Room, Firebase & Logic đồng bộ)
│   ├── 📂 local/ (Cấu hình Database cục bộ - Offline Cache cho sách đã tải)
│   │   ├── 📂 dao/
│   │   │   ├── BookDao.kt (CRUD Sách tải về máy)
│   │   │   ├── UserDao.kt (Lưu thông tin User cục bộ)
│   │   │   └── SearchHistoryDao.kt (Lưu lịch sử tìm kiếm cục bộ của User)
│   │   ├── 📂 entity/
│   │   │   ├── BookEntity.kt (Bảng cấu trúc Sách lưu trong SQLite)
│   │   │   ├── ChapterEntity.kt (Bảng cấu trúc Chương lưu trong SQLite)
│   │   │   └── UserEntity.kt (Bảng cấu trúc Người dùng lưu trong SQLite)
│   │   └── BookDatabase.kt (Khởi tạo và quản lý phiên bản Room Database)
│   │
│   ├── 📂 remote/ (Xử lý kết nối API và Mạng đám mây - Online Server)
│   │   ├── AuthService.kt (Quản lý Auth: Đăng nhập Email/Pass, Google, Firebase OTP)
│   │   ├── FirestoreService.kt (CRUD toàn bộ dữ liệu trên Cloud Firestore)
│   │   └── StorageService.kt (Upload file vật lý: Ảnh bìa truyện, Ảnh đại diện User)
│   │
│   └── 📂 repository/ (Thực thi các Interface định nghĩa ở tầng Domain)
│       ├── AuthRepositoryImpl.kt (Triển khai logic xác thực)
│       ├── BookRepositoryImpl.kt (Logic Hybrid: Check online lấy Firestore và lưu cache Room; offline lấy Room)
│       └── AdminRepositoryImpl.kt (Triển khai các tác vụ quản trị hệ thống)
│
├── 📂 domain/ (TẦNG NGHIỆP VỤ - Lõi hệ thống, không chứa code Android/Firebase)
│   ├── 📂 model/ (Định nghĩa các Class dữ liệu chuẩn hóa để UI hiển thị)
│   │   ├── Book.kt (Model dữ liệu Sách chuẩn)
│   │   ├── Chapter.kt (Model dữ liệu Chương chuẩn)
│   │   ├── Category.kt (Model dữ liệu Thể loại truyện)
│   │   ├── ReadingProgress.kt (Tiến độ đọc, vị trí ghim trang của User)
│   │   └── AnalyticsData.kt (Dữ liệu thống kê biểu đồ đường cho Admin)
│   │
│   └── 📂 repository/ (Định nghĩa các Interface quản lý dữ liệu - Khung điều hướng)
│       ├── AuthRepository.kt (Interface quy định các hàm xác thực)
│       ├── BookRepository.kt (Interface quy định các hàm lấy sách, đọc sách, tương tác)
│       └── AdminRepository.kt (Interface quy định các hàm thống kê, duyệt bài, khóa user)
│
└── 📂 ui/ (TẦNG GIAO DIỆN - Xây dựng hoàn toàn bằng Jetpack Compose)
    ├── 📂 components/ (Các thành phần giao diện nhỏ, tái sử dụng ở nhiều màn hình)
    │   ├── PrimaryButton.kt (Nút bấm chính đồng bộ thiết kế hệ thống)
    │   ├── BookVerticalItem.kt (Thành phần hiển thị thông tin sách dạng hàng dọc)
    │   ├── RecentBookCard.kt (Thẻ truyện nằm ngang hiển thị tại Trang chủ)
    │   ├── ReviewRow.kt (Thành phần hiển thị dòng đánh giá/bình luận công khai)
    │   ├── LineChartComponent.kt (Biểu đồ đường phục vụ thống kê của Admin)
    │   ├── CustomTextField.kt (Ô nhập liệu chuẩn hóa font và màu sắc)
    │   └── RatingBar.kt (Thanh chọn số sao đánh giá từ 1 đến 5)
    │
    ├── 📂 theme/ (Định nghĩa Design System: Màu sắc, font chữ, kiểu dáng)
    │   ├── Color.kt (Bảng màu chuẩn thiết kế)
    │   ├── Type.kt (Cấu hình Typography - Định dạng text hệ thống)
    │   └── Theme.kt (Xử lý Đổi nền tự động: Sáng / Tối Dark Mode / màu nền Sepia)
    │
    ├── 📂 viewmodel/ (Quản lý trạng thái UI State và xử lý sự kiện từ màn hình truyền về)
    │   ├── AuthViewModel.kt (Quản lý logic Đăng nhập, Đăng ký, OTP, Quên mật khẩu)
    │   ├── HomeViewModel.kt (Quản lý dữ liệu phân phối cho Trang chủ và Khám phá)
    │   ├── BookDetailViewModel.kt (Quản lý tương tác chi tiết: Đọc truyện, Bình luận, Đánh giá, Ghim trang)
    │   ├── CreatorViewModel.kt (Quản lý góc sáng tác: Đăng truyện, Thêm chương, Thống kê cá nhân)
    │   └── AdminViewModel.kt (Quản lý biểu đồ tổng quan, duyệt bài, ẩn bình luận, khóa tài khoản)
    │
    ├── 📂 model/
    │   ├── BookUi.kt                  └── Chứa các model đã gọt giũa riêng cho UI
    │   └── CategoryUi.kt              └── (Ví dụ: có thêm trường số lượng chương, số sao)
    │
    └── 📂 screens/ (Giao diện các màn hình chính - Chia nhỏ theo cụm vai trò chuyên biệt)
        │
        ├── 📂 auth/ (Cụm chức năng Xác thực & Tài khoản)
        │   ├── WelcomeScreen.kt (Màn hình chào mừng khi khởi động ứng dụng lần đầu)
        │   ├── AuthOptionsScreen.kt (Màn hình lựa chọn phương thức Đăng nhập/Đăng ký)
        │   ├── LoginScreen.kt (Form đăng nhập hệ thống + Tính năng Ghi nhớ đăng nhập)
        │   ├── RegisterScreen.kt (Form tạo tài khoản mới + Luồng nhận mã xác thực OTP)
        │   ├── ForgotPasswordScreen.kt (Màn hình Quên mật khẩu & Nhập mật khẩu mới)
        │   └── GenreSelectionScreen.kt (Màn hình chọn ít nhất 3 thể loại yêu thích khi vào app lần đầu)
        │   
        ├── 📂 user/ (Cụm chức năng Đọc giả & Quản lý Cá nhân)
        │   ├── HomeScreen.kt (Trang chủ: Hiển thị các Slider truyện Trending, Hot, Đề xuất từ hệ thống)
        │   ├── ExploreScreen.kt (Trang khám phá: Danh sách thể loại và bộ lọc phân loại trạng thái truyện)
        │   ├── SearchScreen.kt (Tìm kiếm nâng cao theo Tác giả/Số sao + Lưu lịch sử, gợi ý từ khóa hot)
        │   ├── ProductListScreen.kt (Màn hình hiển thị danh sách sản phẩm chung khi nhấn nút "Xem thêm")
        │   ├── LibraryScreen.kt (Thư viện cá nhân: Xử lý bộ lọc qua 3 Tab độc lập: Yêu thích, Lịch sử, Đã tải)
        │   ├── BookDetailScreen.kt (Màn hình chi tiết truyện: Thông tin, điểm rating, viết bình luận công khai)
        │   ├── BookReaderScreen.kt (Giao diện đọc nội dung văn bản: Phân trang mượt mà, ghim trang đọc dở)
        │   ├── ProfileScreen.kt (Hồ sơ người dùng: Thống kê số liệu đọc cá nhân, tinh chỉnh font chữ/cỡ nền)
        │   ├── EditProfileScreen.kt (Giao diện chỉnh sửa thông tin cá nhân: Đổi Avatar hiển thị, đổi mật khẩu)
        │   │
        │   └── 📂 creator_studio/ (Cụm chức năng Góc Sáng Tác dành cho mọi User)
        │       ├── AuthorDashboardScreen.kt (Thống kê số liệu sáng tác cá nhân, danh sách truyện đã đăng, bản thảo nháp)
        │       ├── MyBookDetailScreen.kt (Chi tiết truyện của tôi: Hủy bài nếu đang Chờ duyệt; Hoàn thành/Thêm chương nếu Đang chạy)
        │       ├── CreateBookScreen.kt (Giao diện tạo tác phẩm mới: Nhập tên, tóm tắt, chọn tag và upload ảnh bìa lên Storage)
        │       └── AddChapterScreen.kt (Giao diện viết chương mới cho tác phẩm công khai, hỗ trợ tính năng lưu bản thảo nháp)
        │
        └── 📂 admin/ (Cụm chức năng Quản trị và Kiểm duyệt Hệ thống)
            ├── AdminDashboardScreen.kt (Màn hình tổng quan: Vẽ biểu đồ đường lượt đọc chạy theo bộ lọc thời gian chỉ định)
            ├── BookManagementScreen.kt (Quản lý truyện hệ thống: Duyệt bài chờ duyệt, Ẩn/Hiện truyện qua các Tab trạng thái)
            ├── AdminBookDetailScreen.kt (Xem chi tiết thông tin tác phẩm và quản lý danh sách chương dưới góc nhìn Admin)
            ├── CommentReviewScreen.kt (Kiểm duyệt đánh giá: Bộ lọc bình luận khách hàng, nút Ẩn nội dung độc hại và nút Hoàn tác)
            ├── UserManagementScreen.kt (Quản lý tài khoản: Ô tìm kiếm Tên/Email, sắp xếp theo Đang hoạt động/Đã khóa và nút Khóa nhanh)
            └── UserDetailScreen.kt (Chi tiết tài khoản thành viên: Thống kê nhanh thông tin, kiểm tra danh sách các truyện họ đã đăng)
