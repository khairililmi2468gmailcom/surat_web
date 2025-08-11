document.getElementById('registerForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Mencegah form submit secara default

    const form = event.target;
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    
    // Elemen pesan error
    const usernameError = document.getElementById('username-error');
    const emailError = document.getElementById('email-error');
    const passwordError = document.getElementById('password-error');
    const confirmPasswordError = document.getElementById('confirm-password-error');

    let isValid = true;

    // Reset pesan error
    usernameError.textContent = '';
    emailError.textContent = '';
    passwordError.textContent = '';
    confirmPasswordError.textContent = '';

    // Validasi Username
    if (usernameInput.value.trim() === '') {
        usernameError.textContent = 'Username harus diisi.';
        isValid = false;
    }

    // Validasi Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailInput.value)) {
        emailError.textContent = 'Format email tidak valid.';
        isValid = false;
    }

    // Validasi Password (minimal 8 karakter)
    if (passwordInput.value.length < 8) {
        passwordError.textContent = 'Password harus minimal 8 karakter.';
        isValid = false;
    }

    // Validasi Konfirmasi Password
    if (passwordInput.value !== confirmPasswordInput.value) {
        confirmPasswordError.textContent = 'Konfirmasi password tidak cocok.';
        isValid = false;
    }

    if (!isValid) {
        // Jika ada error validasi, tampilkan notifikasi umum
        Swal.fire({
            title: 'Validasi Gagal',
            text: 'Silakan periksa kembali semua input yang diperlukan.',
            icon: 'warning',
            confirmButtonText: 'OK'
        });
        return; // Hentikan proses
    }

    // Jika valid, lanjutkan dengan Fetch
    document.getElementById('loading-overlay').classList.add('show');
    const formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
    .then(response => {
        document.getElementById('loading-overlay').classList.remove('show');
        return response.json().then(data => ({ status: response.status, body: data }));
    })
    .then(({ status, body }) => {
        if (status === 200) { // Sukses
            Swal.fire({
                title: 'Registrasi Berhasil!',
                text: body.message,
                icon: 'success',
                confirmButtonText: 'Login Sekarang'
            }).then(() => {
                // Arahkan ke halaman login dengan parameter success
                window.location.href = '/auth/login?success=true';
            });
        } else { // Gagal (misal: email sudah ada)
            Swal.fire({
                title: 'Registrasi Gagal',
                text: body.message,
                icon: 'error',
                confirmButtonText: 'Coba Lagi'
            });
        }
    })
    .catch(error => {
        document.getElementById('loading-overlay').classList.remove('show');
        Swal.fire({
            title: 'Oops...',
            text: 'Terjadi kesalahan pada server. Silakan coba lagi nanti.',
            icon: 'error'
        });
        console.error('Error:', error);
    });
});

document.getElementById('google-signup-btn').addEventListener('click', function() {
    // Arahkan ke endpoint otorisasi Google yang dibuat oleh Spring Security
    window.location.href = '/oauth2/authorization/google';
});


// Event listener untuk tombol Google (hanya notifikasi)
// document.getElementById('google-signup-btn').addEventListener('click', function() {
//     Swal.fire({
//         title: 'Fitur Dalam Pengembangan',
//         text: 'Registrasi dengan Google akan segera tersedia!',
//         icon: 'info',
//         confirmButtonText: 'Mengerti'
//     });
// });
