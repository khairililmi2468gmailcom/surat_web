// Fungsi untuk membersihkan URL dari parameter
function cleanUrlParams() {
    const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
    window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
}

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.has('logout')) {
         Swal.fire({
            title: 'Berhasil Logout',
            text: 'Anda telah berhasil keluar.',
            icon: 'success',
            confirmButtonText: 'OK'
        }).then(() => {
            cleanUrlParams(); // Hapus ?logout dari URL setelah alert ditutup
        });
    }

    if (urlParams.has('success')) {
        Swal.fire({
            title: 'Registrasi Berhasil!',
            text: 'Akun Anda berhasil dibuat. Silakan login.',
            icon: 'success',
            confirmButtonText: 'OK'
        }).then(() => {
            cleanUrlParams(); // Hapus ?success dari URL setelah alert ditutup
        });
    }
});

// Event listener untuk form login email/password
document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Wajib untuk mencegah submit standar

    const form = event.target;
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const emailError = document.getElementById('email-error');
    const passwordError = document.getElementById('password-error');
    let isValid = true;
    
    emailError.textContent = '';
    passwordError.textContent = '';

    if (emailInput.value.trim() === '') {
        emailError.textContent = 'Email harus diisi.';
        isValid = false;
    }
    if (passwordInput.value.trim() === '') {
        passwordError.textContent = 'Password harus diisi.';
        isValid = false;
    }

    if (!isValid) return;

    document.getElementById('loading-overlay').classList.add('show');
    const formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
    .then(response => {
        document.getElementById('loading-overlay').classList.remove('show');
        if (response.ok) {
            return response.json();
        }
        return response.json().then(errorData => {
             throw new Error(errorData.message || 'Terjadi kesalahan');
        });
    })
    .then(data => {
        if (data.success) {
            Swal.fire({
                title: 'Login Berhasil!',
                text: 'Anda akan dialihkan ke dashboard.',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            }).then(() => {
                window.location.href = data.redirectUrl;
            });
        }
    })
    .catch(error => {
        document.getElementById('loading-overlay').classList.remove('show');
        Swal.fire({
            title: 'Gagal Login',
            text: error.message,
            icon: 'error',
            confirmButtonText: 'Coba Lagi'
        });
    });
});

// --- BARU: Event listener untuk tombol Google Login ---
document.getElementById('google-login-btn').addEventListener('click', function() {
    // Tampilkan loading sebelum redirect
    document.getElementById('loading-overlay').classList.add('show');
    // Arahkan ke endpoint otorisasi Google yang dibuat oleh Spring Security
    window.location.href = '/oauth2/authorization/google';
});