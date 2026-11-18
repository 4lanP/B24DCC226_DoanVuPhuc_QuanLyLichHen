package com.example.quanlylichhen;

import android.app.Activity;
import android.os.Build;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Cấu hình chế độ vẽ tràn viền (edge-to-edge).
 *
 * <p>Từ Android 15, ứng dụng nhắm targetSdk 35 trở lên luôn vẽ tràn ra sau thanh trạng thái
 * và thanh điều hướng. Vì vậy mỗi màn hình phải tự cộng thêm khoảng chừa (insets) cho phần
 * nội dung, và chọn màu icon thanh trạng thái tương ứng với nền phía sau nó.</p>
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * @param lightStatusBarIcons true nếu nền phía sau thanh trạng thái là màu sáng
     *                            (icon sẽ tô màu tối), false nếu nền là màu đậm.
     */
    public static void applyEdgeToEdge(Activity activity, boolean lightStatusBarIcons) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(lightStatusBarIcons);
        // Nền phía dưới cùng của cả hai màn hình đều sáng nên icon thanh điều hướng tô màu tối.
        controller.setAppearanceLightNavigationBars(true);

        // Trước Android 8.0 chưa có icon thanh điều hướng màu tối: dùng nền mờ tối để icon trắng còn thấy.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(0x66000000);
        }
    }
}
