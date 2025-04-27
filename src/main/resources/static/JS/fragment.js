document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById('sidebar');
    const sidebarOpen = document.getElementById('sidebar-open');
    const sidebarToggle = document.getElementById('sidebar-toggle');

    sidebarOpen.addEventListener('click', function () {
        sidebar.classList.remove('-translate-x-full');
    });

    sidebarToggle.addEventListener('click', function () {
        sidebar.classList.add('-translate-x-full');
    });

    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function (event) {
        const isClickInside = sidebar.contains(event.target) || sidebarOpen.contains(event.target);

        if (!isClickInside && window.innerWidth < 768 && !sidebar.classList.contains('-translate-x-full')) {
            sidebar.classList.add('-translate-x-full');
        }
    });

    // Handle responsive behavior
    window.addEventListener('resize', function () {
        if (window.innerWidth >= 768) {
            sidebar.classList.remove('-translate-x-full');
        } else {
            sidebar.classList.add('-translate-x-full');
        }
    });
});

document.getElementById('ml-file-input').addEventListener('change', function () {
    if (this.files.length > 0) {
        this.form.submit();
    }
});