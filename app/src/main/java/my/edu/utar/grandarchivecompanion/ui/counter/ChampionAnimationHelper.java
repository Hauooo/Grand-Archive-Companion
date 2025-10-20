package my.edu.utar.grandarchivecompanion.ui.counter;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import android.os.Handler;

// Helper class for champion animations


public class ChampionAnimationHelper {

    // 🔴 Damage animation: shake + red flash
    public static void playDamage(View imageView) {
        // Shake horizontally
        ObjectAnimator shake = ObjectAnimator.ofFloat(
                imageView, "translationX",
                0, 25, -25, 15, -15, 6, -6, 0
        );

        shake.setDuration(300);
        shake.start();

        flashTint((ImageView) imageView, DAMAGE_TINT);

        // Shake diagonally
        ObjectAnimator shakeY = ObjectAnimator.ofFloat(
                imageView, "translationY",
                0, 15, -15, 10, -10, 5, -5, 0
        );
        shakeY.setDuration(300);
        shakeY.start();


        // Red tint flash
        imageView.animate()
                .alpha(0.7f) // temporarily dim
                .setDuration(100)
                .withEndAction(() -> imageView.animate()
                        .alpha(0.5f)
                        .setDuration(200));
    }

    // 🟢 Heal animation: glow + green flash
    public static void playHeal(View imageView) {
        // Glow effect (scale up and back)
        ObjectAnimator glow = ObjectAnimator.ofFloat(imageView, "scaleX", 1.0f, 1.1f, 1.f);
        ObjectAnimator glowY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.0f, 1.1f, 1.f);
        glow.setDuration(100);
        glowY.setDuration(100);
        glow.start();
        glowY.start();

        flashTint((ImageView) imageView, HEAL_TINT);

        // Optional: green tint flash (quick fade)
        imageView.animate()
                .alpha(0.7f)
                .setDuration(100)
                .withEndAction(() -> imageView.animate()
                        .alpha(0.5f)
                        .setDuration(200));
    }

    private static void flashTint(ImageView champion, int color) {
        champion.setColorFilter(color);

        ObjectAnimator animator = ObjectAnimator.ofInt(champion, "colorFilter", color, Color.TRANSPARENT);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setDuration(300);
        animator.start();

        new Handler().postDelayed(() -> champion.clearColorFilter(), 300);
    }

    // 50% transparent red and green
    private static final int DAMAGE_TINT = Color.argb(128, 255, 0, 0);   // semi-transparent red
    private static final int HEAL_TINT   = Color.argb(128, 0, 255, 0);   // semi-transparent green

}



