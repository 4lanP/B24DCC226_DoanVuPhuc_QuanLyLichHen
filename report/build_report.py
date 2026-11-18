# -*- coding: utf-8 -*-
"""Bộ dựng báo cáo DOCX cho bài thực hành Android "Quản lý lịch hẹn".

Dùng chính file mẫu của giảng viên làm nền (giữ nguyên style Heading/Normal và khối
tiêu đề trường), sau đó xoá nội dung cũ và sinh nội dung mới.

Chạy:  python build_report.py
"""
import copy
import os
import sys

import docx
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt

HERE = os.path.dirname(os.path.abspath(__file__))
TEMPLATE = r"C:\Users\Admin\Downloads\B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx"
ASSETS = os.path.join(HERE, "assets")
OUT_DOCX = os.path.join(HERE, "B24DCCC226_Đoàn Vũ Phúc_Báo cáo.docx")

BULLET_ID = 8    # numId bullet cấp 0 trong template ("•")
SUB_BULLET_ID = 10  # numId bullet cấp con ("o")


# --------------------------------------------------------------------------
# Hàm tiện ích
# --------------------------------------------------------------------------
def clear_body(document):
    """Xoá toàn bộ nội dung body, giữ lại sectPr (khổ giấy, lề)."""
    body = document.element.body
    for child in list(body):
        if child.tag == qn("w:sectPr"):
            continue
        body.remove(child)


def keep_first_table(document):
    """Lấy bản sao bảng tiêu đề trường trước khi xoá nội dung."""
    if not document.tables:
        return None
    return copy.deepcopy(document.tables[0]._tbl)


def set_paragraph_text(paragraph, text):
    """Đổi nội dung đoạn văn nhưng giữ định dạng của run đầu tiên."""
    runs = paragraph.runs
    if not runs:
        paragraph.add_run(text)
        return
    runs[0].text = text
    for run in runs[1:]:
        run._r.getparent().remove(run._r)


def add_bullet(document, text, level=0, bold=False):
    """Thêm một gạch đầu dòng dùng numPr của template."""
    para = document.add_paragraph(style="List Paragraph")
    pPr = para._p.get_or_add_pPr()
    numPr = OxmlElement("w:numPr")
    ilvl = OxmlElement("w:ilvl")
    ilvl.set(qn("w:val"), "0")
    numId = OxmlElement("w:numId")
    numId.set(qn("w:val"), str(SUB_BULLET_ID if level else BULLET_ID))
    numPr.append(ilvl)
    numPr.append(numId)
    pPr.append(numPr)
    run = para.add_run(text)
    run.bold = bold
    return para


def add_heading_keep(document, text, level):
    h = document.add_heading(text, level=level)
    h.paragraph_format.keep_with_next = True
    return h


def add_para(document, text="", bold=False, italic=False, align=None, size=None,
             space_before=None, space_after=None, style="Normal"):
    para = document.add_paragraph(style=style)
    if align is not None:
        para.alignment = align
    if space_before is not None:
        para.paragraph_format.space_before = Pt(space_before)
    if space_after is not None:
        para.paragraph_format.space_after = Pt(space_after)
    if text:
        run = para.add_run(text)
        run.bold = bold
        run.italic = italic
        if size:
            run.font.size = Pt(size)
    return para


def add_figure(document, filename, caption, width_cm=4.4):
    path = os.path.join(ASSETS, filename)
    if not os.path.exists(path):
        print("THIEU ANH:", path)
        return
    para = document.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.paragraph_format.space_before = Pt(4)
    para.paragraph_format.space_after = Pt(2)
    para.paragraph_format.line_spacing = 1.0
    para.paragraph_format.keep_with_next = True
    para.add_run().add_picture(path, width=Cm(width_cm))

    cap = document.add_paragraph()
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap.paragraph_format.space_before = Pt(0)
    cap.paragraph_format.space_after = Pt(6)
    run = cap.add_run(caption)
    run.font.size = Pt(11)
    run.italic = True


def set_cell_borders(cell, color="808080", size="4"):
    tcPr = cell._tc.get_or_add_tcPr()
    borders = OxmlElement("w:tcBorders")
    for edge in ("top", "left", "bottom", "right"):
        el = OxmlElement("w:" + edge)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), size)
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), color)
        borders.append(el)
    tcPr.append(borders)


def shade_cell(cell, fill="EFEFEF"):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), fill)
    tcPr.append(shd)


def add_table(document, headers, rows, widths_cm, size=10, caption=None):
    if caption:
        cap = document.add_paragraph()
        cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap.paragraph_format.space_before = Pt(8)
        cap.paragraph_format.space_after = Pt(3)
        run = cap.add_run(caption)
        run.bold = True
        run.font.size = Pt(10.5)

    table = document.add_table(rows=1, cols=len(headers))
    table.style = "Normal Table"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    hdr = table.rows[0].cells
    for i, text in enumerate(headers):
        hdr[i].text = ""
        para = hdr[i].paragraphs[0]
        para.paragraph_format.space_after = Pt(1)
        run = para.add_run(text)
        run.bold = True
        run.font.size = Pt(size)
        shade_cell(hdr[i])
        set_cell_borders(hdr[i])

    for row in rows:
        cells = table.add_row().cells
        for i, text in enumerate(row):
            cells[i].text = ""
            para = cells[i].paragraphs[0]
            para.paragraph_format.space_after = Pt(1)
            run = para.add_run(str(text))
            run.font.size = Pt(size)
            set_cell_borders(cells[i])

    for row in table.rows:
        for i, width in enumerate(widths_cm):
            row.cells[i].width = Cm(width)

    # ghi đè tblGrid để Word/LibreOffice chia cột đúng
    tblGrid = table._tbl.find(qn("w:tblGrid"))
    if tblGrid is not None:
        for gridCol, width in zip(tblGrid.findall(qn("w:gridCol")), widths_cm):
            gridCol.set(qn("w:w"), str(int(width * 567)))

    add_para(document, "", space_after=4)
    return table


# --------------------------------------------------------------------------
# Nội dung báo cáo
# --------------------------------------------------------------------------
def build_title(document, title_tbl):
    if title_tbl is not None:
        # chèn vào ĐẦU body (trước sectPr), nếu không bảng sẽ rơi xuống cuối tài liệu
        document.element.body.insert(0, title_tbl)
        table = document.tables[0]
        cell = table.rows[0].cells[0]

        for para in cell.paragraphs:
            text = para.text.strip()
            if text.startswith("Bài tập thực hành"):
                set_paragraph_text(para, "Bài tập thực hành: Ứng dụng Quản lý lịch hẹn")
            elif text.startswith("Hà Nội"):
                set_paragraph_text(para, "Hà Nội, ngày 18 tháng 11 năm 2026")
                # thêm dòng sinh viên ngay trước dòng ngày
                for label in ("Sinh viên thực hiện: Đoàn Vũ Phúc",
                              "Mã sinh viên: B24DCCC226"):
                    new_para = para.insert_paragraph_before()
                    new_para.alignment = WD_ALIGN_PARAGRAPH.CENTER
                    run = new_para.add_run(label)
                    run.bold = True
                    run.font.size = Pt(16)
                    run.font.name = "Times New Roman"


def build_intro(document):
    add_heading_keep(document, "Giới thiệu ứng dụng", level=1)
    add_para(document, "Ứng dụng Android Quản lý lịch hẹn giúp người dùng ghi lại các "
                       "cuộc hẹn, cuộc họp cá nhân kèm địa điểm, thời gian và trạng thái "
                       "nhắc nhở. Bài làm gồm bốn màn hình: đăng nhập, danh sách lịch hẹn, "
                       "thêm lịch hẹn và chi tiết lịch hẹn.")
    add_bullet(document, "Ngôn ngữ và giao diện: Java và XML, giao diện Material 3, "
                         "chuyển màn hình bằng Intent.", bold=False)
    add_bullet(document, "Lưu trữ: toàn bộ dữ liệu nằm trong SQLite thông qua lớp "
                         "DatabaseHelper kế thừa SQLiteOpenHelper; không dùng Room, "
                         "Firebase hay API mạng.")
    add_bullet(document, "Mô hình dữ liệu: lớp Appointment biểu diễn một lịch hẹn; "
                         "ArrayList chỉ dùng để chứa kết quả truy vấn hiển thị lên giao diện.")
    add_bullet(document, "Điều hướng: màn hình chi tiết nhận id qua Intent rồi truy vấn "
                         "lại cơ sở dữ liệu, không truyền cả đối tượng.")
    add_bullet(document, "Tài khoản mẫu: admin / 123456; dữ liệu mẫu gồm ba lịch hẹn ngày "
                         "25, 26 và 27 tháng 11 năm 2026.")


def build_login(document):
    add_heading_keep(document, "Màn hình đăng nhập", level=1)
    add_heading_keep(document, "Hình ảnh minh chứng", level=2)
    add_figure(document, "fig1_1_login.png", "1.1.Hình đăng nhập")
    add_figure(document, "fig1_2_login_empty.png", "1.2.Lỗi khi chưa nhập mật khẩu")
    add_figure(document, "fig1_3_login_wrong.png", "1.3.Lỗi khi nhập sai tên đăng nhập/mật khẩu")

    add_heading_keep(document, "Mô tả kỹ thuật nội dung đã làm", level=2)
    add_bullet(document, "Các thành phần giao diện (UI Components):", bold=True)
    add_bullet(document, "TextInputLayout kèm TextInputEditText (edtUsername): nhập tên "
                         "đăng nhập, có icon và gợi ý bên trong ô.", level=1)
    add_bullet(document, "TextInputLayout kèm TextInputEditText (edtPassword): nhập mật "
                         "khẩu, dùng android:inputType=\"textPassword\" để ẩn ký tự và "
                         "endIconMode=\"password_toggle\" để bật/tắt hiển thị.", level=1)
    add_bullet(document, "TextView (tvError): hiển thị thông báo lỗi màu đỏ, mặc định ẩn "
                         "bằng View.GONE, có icon cảnh báo.", level=1)
    add_bullet(document, "MaterialButton (btnLogin): nút Đăng nhập, bo góc 14dp, gọi hàm "
                         "checkLogin().", level=1)

    add_bullet(document, "Kiểm tra tài khoản bằng cơ sở dữ liệu:", bold=True)
    add_bullet(document, "Thông tin đăng nhập không cố định trong code của Activity mà "
                         "truy vấn bảng users qua DatabaseHelper.checkLogin(username, "
                         "password).", level=1)
    add_bullet(document, "Câu truy vấn dùng tham số: selection \"username = ? AND "
                         "password = ?\" kèm selectionArgs, không ghép chuỗi.", level=1)
    add_bullet(document, "Cursor được đóng bằng try-with-resources; hàm trả về "
                         "cursor.moveToFirst() nên chỉ cần biết có dòng khớp hay không.",
               level=1)
    add_bullet(document, "Tài khoản mẫu admin / 123456 được chèn trong onCreate() của "
                         "DatabaseHelper.insertUser().", level=1)

    add_bullet(document, "Xử lý sự kiện và kiểm tra dữ liệu:", bold=True)
    add_bullet(document, "Bắt sự kiện click trên nút btnLogin, gọi checkLogin() để kiểm "
                         "tra dữ liệu nhập.", level=1)
    add_bullet(document, "Dùng trim() rồi kiểm tra rỗng: bỏ trống tên đăng nhập hiện "
                         "\"Vui lòng nhập tên đăng nhập\", bỏ trống mật khẩu hiện \"Vui "
                         "lòng nhập mật khẩu\"; trường hợp này không truy vấn cơ sở dữ liệu.",
               level=1)
    add_bullet(document, "Sai tài khoản: hiển thị \"Sai tên đăng nhập hoặc mật khẩu\", xoá "
                         "ô mật khẩu và giữ nguyên màn hình đăng nhập.", level=1)
    add_bullet(document, "Đúng tài khoản: khởi tạo Intent sang MainActivity và gọi "
                         "finish() để nút Back không quay lại màn hình đăng nhập.", level=1)
    add_bullet(document, "TextWatcher trên cả hai ô: khi người dùng sửa lại nội dung thì "
                         "thông báo lỗi cũ tự ẩn đi.", level=1)
    add_bullet(document, "Bàn phím được ẩn trước khi chuyển màn hình; giao diện vẽ tràn "
                         "viền và tự cộng khoảng chừa thanh trạng thái qua UiUtils.", level=1)


def build_main(document):
    add_heading_keep(document, "Màn hình danh sách lịch hẹn (MainActivity)", level=1)
    add_heading_keep(document, "Hình ảnh minh chứng", level=2)
    add_figure(document, "fig2_1_list.png", "2.1.Danh sách và thống kê")
    add_figure(document, "fig2_2_empty.png", "2.2.Trạng thái chưa có lịch hẹn nào")
    add_figure(document, "fig2_3_after_add.png", "2.3.Sau khi thêm lịch hẹn mới, thống kê cập nhật")

    add_heading_keep(document, "Mô tả kỹ thuật nội dung đã làm", level=2)
    add_bullet(document, "Các thành phần giao diện:", bold=True)
    add_bullet(document, "Phần đầu (header) nền chuyển sắc teal, hiển thị tiêu đề "
                         "\"Lịch hẹn của tôi\" và dòng phụ \"Sắp xếp theo thời gian gần nhất\".",
               level=1)
    add_bullet(document, "CardView khối thống kê gồm hai TextView: tvStatTotal (tổng số "
                         "lịch hẹn) và tvStatReminders (số lịch đang bật nhắc nhở). Thẻ "
                         "được neo đỉnh vào đáy header với layout_marginTop=\"-56dp\" nên "
                         "đè qua ranh giới header đúng như phác thảo.", level=1)
    add_bullet(document, "ListView (listAppointments): danh sách lịch hẹn, mỗi dòng hiển "
                         "thị tên, địa điểm, ngày giờ dạng dd/MM/yyyy · HH:mm và nhãn "
                         "\"Có nhắc\"/\"Không nhắc\".", level=1)
    add_bullet(document, "MaterialButton (btnAdd): nút \"+ Thêm lịch hẹn\" ghim ở cuối "
                         "màn hình.", level=1)
    add_bullet(document, "LinearLayout (emptyState): khối thông báo khi chưa có lịch hẹn "
                         "nào, gắn với ListView qua setEmptyView().", level=1)

    add_bullet(document, "Model và Adapter:", bold=True)
    add_bullet(document, "Lớp Appointment.java biểu diễn một lịch hẹn gồm id, title, "
                         "location, date, time, reminder cùng các hàm get/set.", level=1)
    add_bullet(document, "Lớp AppointmentAdapter.java kế thừa ArrayAdapter<Appointment> "
                         "kết hợp layout item_appointment.xml; dùng ViewHolder để không "
                         "phải findViewById lại khi cuộn.", level=1)
    add_bullet(document, "Mỗi lần getView() gán lại đủ bốn trường và đổi nền nhãn theo "
                         "reminder (bg_chip_on / bg_chip_off), tránh lỗi tái sử dụng view "
                         "hiển thị dữ liệu của dòng khác.", level=1)
    add_bullet(document, "Adapter nhận OnAppointmentClickListener để báo sự kiện chọn dòng "
                         "về MainActivity.", level=1)

    add_bullet(document, "Đọc dữ liệu và tính thống kê:", bold=True)
    add_bullet(document, "getAllAppointments() truy vấn toàn bộ bảng với ORDER BY date ASC, "
                         "time ASC nên danh sách tự sắp xếp theo thời gian gần nhất trước.",
               level=1)
    add_bullet(document, "countAll() dùng SELECT COUNT(*) và countReminders() dùng COUNT "
                         "với điều kiện reminder = 1 để đổ vào khối thống kê.", level=1)
    add_bullet(document, "Toàn bộ việc tải lại nằm trong onResume(): sau khi thêm, xoá "
                         "hoặc đổi nhắc nhở ở màn hình khác, danh sách và thống kê tự cập "
                         "nhật mà không cần khởi động lại ứng dụng.", level=1)

    add_bullet(document, "Xử lý sự kiện:", bold=True)
    add_bullet(document, "Nhấn nút \"+ Thêm lịch hẹn\": gọi startActivity(new "
                         "Intent(MainActivity.this, AddAppointmentActivity.class)).", level=1)
    add_bullet(document, "Nhấn vào một dòng: gửi id qua Intent bằng putExtra("
                         "EXTRA_APPOINTMENT_ID, appointment.getId()) rồi mở "
                         "AppointmentDetailActivity.", level=1)


def build_add(document):
    add_heading_keep(document, "Màn hình Thêm lịch hẹn (AddAppointmentActivity)", level=1)
    add_heading_keep(document, "Hình ảnh minh chứng", level=2)
    add_figure(document, "fig3_1_add.png", "3.1.Giao diện thêm lịch hẹn")
    add_figure(document, "fig3_2_add_empty.png", "3.2.Lỗi khi tên lịch hẹn để trống")
    add_figure(document, "fig3_3_add_past.png", "3.3.Lỗi khi thời điểm hẹn ở quá khứ")

    add_heading_keep(document, "Mô tả kỹ thuật nội dung đã làm", level=2)
    add_bullet(document, "Các thành phần giao diện:", bold=True)
    add_bullet(document, "TextInputLayout kèm EditText (edtTitle): nhập tên lịch hẹn "
                         "(bắt buộc), có gợi ý ví dụ.", level=1)
    add_bullet(document, "TextInputLayout kèm EditText (edtLocation): nhập địa điểm "
                         "(bắt buộc).", level=1)
    add_bullet(document, "DatePicker (datePicker): chọn ngày hẹn theo dạng ba cột "
                         "tháng/ngày/năm.", level=1)
    add_bullet(document, "TimePicker (timePicker): chọn giờ hẹn theo chế độ spinner hai "
                         "cột giờ : phút, khung 24 giờ.", level=1)
    add_bullet(document, "MaterialSwitch (switchReminder): bật/tắt nhắc nhở trước giờ hẹn "
                         "(chỉ lưu trạng thái).", level=1)
    add_bullet(document, "MaterialButton (btnSave): nút \"Lưu lịch hẹn\" ghim ở cuối màn "
                         "hình, nằm ngoài vùng cuộn nên luôn nhìn thấy đủ.", level=1)
    add_bullet(document, "TextView (tvTimeError): dòng thông báo đỏ \"Thời điểm hẹn phải "
                         "sau thời điểm hiện tại\", mặc định ẩn.", level=1)

    add_bullet(document, "Cấu hình picker theo đúng yêu cầu đề bài:", bold=True)
    add_bullet(document, "Từ Android 5.0, DatePicker và TimePicker theo theme Material "
                         "luôn hiển thị dạng lịch và đồng hồ tròn, bỏ qua thuộc tính "
                         "spinnersShown/timePickerMode. Vì vậy hai view này được ép "
                         "android:theme=\"@android:style/Theme.Holo.Light\" để hiện đúng "
                         "kiểu spinner như đề yêu cầu.", level=1)
    add_bullet(document, "Khung 24 giờ bật bằng timePicker.setIs24HourView(true) trong "
                         "code, vì thuộc tính XML android:is24HourView không tồn tại.", level=1)

    add_bullet(document, "Kiểm tra dữ liệu (Validation):", bold=True)
    add_bullet(document, "Kiểm tra rỗng cho tên và địa điểm, hiển thị lỗi ngay tại ô bằng "
                         "setError() kèm requestFocus(), không lưu dữ liệu.", level=1)
    add_bullet(document, "Ghép ngày theo định dạng yyyy-MM-dd bằng String.format với "
                         "datePicker.getMonth() + 1 (vì getMonth() trả 0–11) và giờ theo "
                         "HH:mm.", level=1)
    add_bullet(document, "So sánh thời điểm: đổi ngày giờ vừa chọn thành Calendar qua "
                         "DateTimeUtils.toCalendar(), đặt SECOND và MILLISECOND bằng 0 "
                         "cho cả hai mốc rồi dùng after() để so tới phút.", level=1)
    add_bullet(document, "Nếu không hợp lệ: hiện dòng lỗi đỏ và cuộn tới cuối form để "
                         "người dùng thấy, không lưu.", level=1)

    add_bullet(document, "Lưu dữ liệu:", bold=True)
    add_bullet(document, "Khi dữ liệu hợp lệ, tạo đối tượng Appointment mới với trạng thái "
                         "lấy từ switchReminder.isChecked().", level=1)
    add_bullet(document, "Gọi dbHelper.insertAppointment(appointment) để ghi vào SQLite; "
                         "hàm trả về id vừa thêm do SQLite sinh ra.", level=1)
    add_bullet(document, "Hiển thị Toast \"Đã lưu lịch hẹn mới\" và gọi finish() để quay "
                         "về danh sách; MainActivity.onResume() tải lại danh sách và "
                         "thống kê nên lịch mới xuất hiện đúng vị trí.", level=1)


def build_detail(document):
    add_heading_keep(document, "Màn hình Chi tiết lịch hẹn (AppointmentDetailActivity)", level=1)
    add_heading_keep(document, "Hình ảnh minh chứng", level=2)
    add_figure(document, "fig4_1_detail_off.png", "4.1.Chi tiết lịch hẹn, trạng thái \"Đang tắt\"")
    add_figure(document, "fig4_2_detail_on.png", "4.2.Sau khi bật nhắc nhở, trạng thái đổi thành \"Đang bật\"")
    add_figure(document, "fig4_3_delete.png", "4.3.Hộp thoại xác nhận xoá lịch hẹn")

    add_heading_keep(document, "Mô tả chi tiết kỹ thuật đã thực hiện", level=2)
    add_bullet(document, "Các thành phần giao diện (UI Components):", bold=True)
    add_bullet(document, "TextView tiêu đề (tvDetailTitle): tên lịch hẹn, chữ lớn in đậm.",
               level=1)
    add_bullet(document, "TextView địa điểm (tvDetailLocation) và TextView thời gian "
                         "(tvDetailDateTime): thời gian hiển thị dạng dd/MM/yyyy · HH:mm.",
               level=1)
    add_bullet(document, "MaterialSwitch (switchReminder) kèm TextView (tvReminderState): "
                         "công tắc nhắc nhở và dòng chữ \"Đang bật\"/\"Đang tắt\".", level=1)
    add_bullet(document, "MaterialButton (btnBackToList): nút \"Quay lại\"; MaterialButton "
                         "(btnDelete): nút \"Xóa lịch hẹn\".", level=1)

    add_bullet(document, "Nhận dữ liệu qua Intent:", bold=True)
    add_bullet(document, "MainActivity chỉ gửi kèm id qua putExtra(EXTRA_APPOINTMENT_ID, "
                         "id); màn hình chi tiết đọc bằng getIntExtra(EXTRA_APPOINTMENT_ID, "
                         "-1).", level=1)
    add_bullet(document, "Gọi dbHelper.getAppointmentById(id) để truy vấn lại cơ sở dữ "
                         "liệu, không truyền cả đối tượng qua Intent.", level=1)
    add_bullet(document, "Nếu trả về null (id không tồn tại), hiển thị Toast \"Không tìm "
                         "thấy lịch hẹn\" và finish() để tránh lỗi ứng dụng.", level=1)

    add_bullet(document, "Xử lý sự kiện bật/tắt nhắc nhở:", bold=True)
    add_bullet(document, "Khi mở màn hình: gỡ listener bằng setOnCheckedChangeListener(null) "
                         "rồi mới setChecked() để không ghi cơ sở dữ liệu lúc vừa hiển thị.",
               level=1)
    add_bullet(document, "Sau đó gắn lại listener: mỗi lần gạt công tắc gọi "
                         "dbHelper.updateReminder(id, isChecked) ghi ngay vào SQLite, cập "
                         "nhật đối tượng trong bộ nhớ và đổi dòng chữ \"Đang bật\"/"
                         "\"Đang tắt\".", level=1)
    add_bullet(document, "Quay về danh sách, onResume() chạy lại nên nhãn \"Có nhắc\" và số "
                         "lịch đang nhắc nhở trong khối thống kê được tính lại.", level=1)

    add_bullet(document, "Xử lý sự kiện xoá lịch hẹn:", bold=True)
    add_bullet(document, "Nhấn \"Xóa lịch hẹn\": hiện AlertDialog.Builder với tiêu đề, nội "
                         "dung \"Bạn có chắc muốn xóa lịch hẹn này?\" và hai nút Hủy / Xóa "
                         "để tránh xoá nhầm.", level=1)
    add_bullet(document, "Khi bấm \"Xóa\": gọi dbHelper.deleteAppointment(id), hiển thị "
                         "Toast \"Đã xóa lịch hẹn\" rồi finish().", level=1)
    add_bullet(document, "MainActivity.onResume() loại bỏ dòng vừa xoá khỏi danh sách và "
                         "giảm số trong khối thống kê.", level=1)
    add_bullet(document, "Nút \"Quay lại\" và nút back trên header đều gọi finish() để trở "
                         "về màn hình danh sách.", level=1)


def build_database(document):
    add_heading_keep(document, "Cơ sở dữ liệu và lớp DatabaseHelper", level=1)
    add_para(document, "Dữ liệu của ứng dụng nằm trong cơ sở dữ liệu appointments.db, "
                       "phiên bản 1, được tạo bởi lớp DatabaseHelper kế thừa "
                       "SQLiteOpenHelper. Bảng và dữ liệu mẫu được tạo trong onCreate().")

    add_table(document,
              ["Cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
              [["id", "INTEGER", "PRIMARY KEY AUTOINCREMENT", "Mã tài khoản"],
               ["username", "TEXT", "NOT NULL, UNIQUE", "Tên đăng nhập"],
               ["password", "TEXT", "NOT NULL", "Mật khẩu (không yêu cầu mã hoá)"]],
              widths_cm=[2.6, 3.0, 5.4, 5.2],
              size=10,
              caption="Bảng 1. Cấu trúc bảng users")

    add_table(document,
              ["Cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
              [["id", "INTEGER", "PRIMARY KEY AUTOINCREMENT", "Mã lịch hẹn"],
               ["title", "TEXT", "NOT NULL", "Tên lịch hẹn"],
               ["location", "TEXT", "NOT NULL", "Địa điểm"],
               ["date", "TEXT", "NOT NULL", "Ngày hẹn, dạng yyyy-MM-dd"],
               ["time", "TEXT", "NOT NULL", "Giờ hẹn, dạng HH:mm"],
               ["reminder", "INTEGER", "NOT NULL", "1: bật nhắc nhở, 0: tắt"]],
              widths_cm=[2.6, 3.0, 5.4, 5.2],
              size=10,
              caption="Bảng 2. Cấu trúc bảng appointments")

    add_para(document, "Ngày và giờ được lưu dạng chuỗi có định dạng cố định nên câu truy "
                       "vấn ORDER BY date ASC, time ASC cho đúng thứ tự thời gian mà không "
                       "cần hàm chuyển đổi.")

    add_table(document,
              ["Phương thức", "Chức năng"],
              [["checkLogin(username, password)", "Trả về true nếu có dòng khớp trong bảng users."],
               ["insertUser(username, password)", "Thêm tài khoản mới vào bảng users."],
               ["insertAppointment(Appointment a)", "Thêm lịch hẹn, trả về id vừa thêm."],
               ["getAllAppointments()", "Lấy toàn bộ lịch hẹn, ORDER BY date ASC, time ASC."],
               ["getAppointmentById(int id)", "Lấy một lịch hẹn theo id."],
               ["updateReminder(int id, boolean on)", "Cập nhật trạng thái nhắc nhở."],
               ["deleteAppointment(int id)", "Xoá lịch hẹn theo id."],
               ["countAll()", "Tổng số lịch hẹn: COUNT(*)."],
               ["countReminders()", "Số lịch đang bật nhắc nhở: COUNT với reminder = 1."]],
              widths_cm=[6.0, 10.2],
              size=10,
              caption="Bảng 3. Các phương thức của DatabaseHelper")

    add_para(document, "Các yêu cầu về lưu trữ đã được xử lý:", bold=True, space_before=6)
    add_bullet(document, "Mọi câu truy vấn dùng tham số ? kèm selectionArgs, không ghép "
                         "chuỗi; Cursor được đóng bằng try-with-resources.")
    add_bullet(document, "Dữ liệu chỉ đọc và ghi qua DatabaseHelper; ArrayList chỉ chứa kết "
                         "quả truy vấn để hiển thị lên ListView.")
    add_bullet(document, "Sau khi thêm, cập nhật hoặc xoá, màn hình danh sách tải lại dữ "
                         "liệu trong onResume().")
    add_bullet(document, "Tắt hẳn ứng dụng rồi mở lại, dữ liệu đã thêm, sửa, xoá vẫn được "
                         "giữ nguyên vì nằm trong tệp SQLite của ứng dụng.")


# --------------------------------------------------------------------------
def build_conclusion(document):
    add_heading_keep(document, "Kết luận", level=1)
    add_para(document, "Bài thực hành đã hoàn thành bốn màn hình theo yêu cầu của đề: đăng "
                       "nhập, danh sách lịch hẹn kèm thống kê, thêm lịch hẹn và chi tiết "
                       "lịch hẹn. Toàn bộ dữ liệu được lưu trong SQLite thông qua lớp "
                       "DatabaseHelper, không dùng ArrayList làm nơi lưu trữ chính và không "
                       "dùng Room, Firebase hay API mạng.")
    add_bullet(document, "Các yêu cầu chính đã đáp ứng: kiểm tra đăng nhập bằng truy vấn "
                         "bảng users; sắp xếp danh sách bằng ORDER BY date, time; thống kê "
                         "tổng số lịch và số lịch đang nhắc nhở; kiểm tra dữ liệu khi thêm "
                         "(bắt buộc nhập, thời điểm phải sau hiện tại); bật/tắt nhắc nhở ghi "
                         "ngay vào cơ sở dữ liệu; xoá lịch hẹn có xác nhận.")
    add_bullet(document, "Kỹ năng thu được: làm việc với SQLiteOpenHelper và Cursor, dùng "
                         "truy vấn có tham số, truyền id qua Intent, quản lý vòng đời "
                         "Activity bằng onResume(), xử lý giao diện tràn viền và bàn phím.")
    add_bullet(document, "Hạn chế: chưa có màn hình sửa đầy đủ nội dung lịch hẹn (đề chỉ yêu "
                         "cầu cập nhật trạng thái nhắc nhở) và nhắc nhở mới dừng ở việc lưu "
                         "trạng thái, chưa tạo thông báo thật.")


def main():
    if not os.path.exists(TEMPLATE):
        print("KHONG THAY FILE MAU:", TEMPLATE)
        sys.exit(1)

    document = docx.Document(TEMPLATE)
    title_tbl = keep_first_table(document)
    clear_body(document)

    build_title(document, title_tbl)
    build_intro(document)
    build_login(document)
    build_main(document)
    build_add(document)
    build_detail(document)
    build_database(document)
    build_conclusion(document)

    document.save(OUT_DOCX)
    print("Da tao:", OUT_DOCX)


if __name__ == "__main__":
    main()
