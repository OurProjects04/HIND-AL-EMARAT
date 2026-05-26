/* ==========================================
   HIND AL EMARAT - Luxury Perfume Store
   Global JavaScript Interactions
   ========================================== */

document.addEventListener('DOMContentLoaded', function() {
    // 1. Navbar Scroll Effect
    const header = document.querySelector('.header-nav');
    window.addEventListener('scroll', function() {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });

    // 2. Mobile Menu Toggle
    const menuToggle = document.querySelector('.menu-toggle');
    const navMenu = document.querySelector('.nav-menu');

    if (menuToggle && navMenu) {
        menuToggle.addEventListener('click', function() {
            navMenu.classList.toggle('open');
            // Toggle hamburger icon if needed
            if (navMenu.classList.contains('open')) {
                menuToggle.innerHTML = '&#10005;'; // Close symbol X
            } else {
                menuToggle.innerHTML = '&#9776;'; // Hamburger symbol
            }
        });
    }

    // 3. Auto-dismiss Alerts
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.6s ease';
            alert.style.opacity = '0';
            setTimeout(function() {
                alert.remove();
            }, 600);
        }, 5000);
    });

    // 4. Scroll Reveal Animations (Intersection Observer)
    const fadeElements = document.querySelectorAll('.glass-card, .section-title, .hero-content');
    if ('IntersectionObserver' in window) {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const revealObserver = new IntersectionObserver(function(entries, observer) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fadeInUp');
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        fadeElements.forEach(function(el) {
            revealObserver.observe(el);
        });
    } else {
        // Fallback for older browsers
        fadeElements.forEach(function(el) {
            el.classList.add('animate-fadeInUp');
        });
    }
});

// Delete Confirmation
function confirmDelete(message) {
    return confirm(message || 'Are you sure you want to delete this item?');
}
