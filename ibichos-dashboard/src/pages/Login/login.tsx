// src/pages/Login/Login.tsx
import { useLogin } from './useLogin';

export function Login() {
  const {
    setEmail,
    setPassword,
    error,
    successMsg,
    rememberMe,
    setRememberMe,
    handleLogin,
    handleResetPassword
  } = useLogin();

  return (
    <div className="d-flex justify-content-center align-items-center vh-100" style={{ backgroundColor: '#E8F5E9' }}>
      <div className="card border-0 shadow-lg" style={{ width: '100%', maxWidth: '420px', borderRadius: '1.5rem', overflow: 'hidden' }}>
        <div className="card-header border-0 text-center pt-5 pb-3 bg-white">
          <img src="/logo_oficial.png" alt="iBichos Logo" className="img-fluid mb-3 drop-shadow" style={{ maxHeight: '110px' }} />
          <h4 className="fw-bolder text-dark mb-0" style={{ letterSpacing: '-0.5px' }}>Administración iBichos</h4>
          <p className="text-secondary small mb-0 mt-1">Acceso seguro para moderadores</p>
        </div>
        <div className="card-body p-4 p-md-5 bg-white pt-2">
          <form onSubmit={handleLogin}>
            {error && (
              <div className="alert alert-danger py-2 border-0 rounded-3 text-center small fw-bold mb-4" role="alert" style={{ backgroundColor: '#ffebee', color: '#c62828' }}>
                {error}
              </div>
            )}
            {successMsg && (
              <div className="alert alert-success py-2 border-0 rounded-3 text-center small fw-bold mb-4" role="alert" style={{ backgroundColor: '#e8f5e9', color: '#2e7d32' }}>
                {successMsg}
              </div>
            )}
            <div className="mb-3">
              <label className="form-label text-muted small fw-bold mb-1">Correo Electrónico</label>
              <input 
                type="email" 
                className="form-control form-control-lg bg-light border-0 shadow-none"
                placeholder="admin@ibichos.com" 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                style={{ fontSize: '15px', borderRadius: '0.8rem' }}
              />
            </div>
            <div className="mb-4">
              <label className="form-label text-muted small fw-bold mb-1">Contraseña</label>
              <input 
                type="password" 
                className="form-control form-control-lg bg-light border-0 shadow-none"
                placeholder="••••••••" 
                onChange={(e) => setPassword(e.target.value)} 
                required 
                style={{ fontSize: '15px', borderRadius: '0.8rem' }}
              />
            </div>
            
            <div className="form-check mb-4 d-flex align-items-center">
              <input 
                className="form-check-input me-2 shadow-none" 
                type="checkbox" 
                id="rememberMe" 
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                style={{ cursor: 'pointer', accentColor: '#3DDC84' }}
              />
              <label className="form-check-label text-muted small fw-medium mt-1" htmlFor="rememberMe" style={{ cursor: 'pointer' }}>
                Mantener sesión iniciada
              </label>
            </div>

            <button 
              type="submit" 
              className="btn w-100 py-3 rounded-3 fw-bold text-white shadow"
              style={{ backgroundColor: '#3DDC84', border: 'none', fontSize: '16px', borderRadius: '0.8rem', transition: 'all 0.3s' }}
              onMouseOver={(e) => {
                e.currentTarget.style.backgroundColor = '#2ebc6e';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.backgroundColor = '#3DDC84';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              Entrar al Panel
            </button>
            <div className="text-center mt-4">
              <button 
                type="button" 
                className="btn btn-link text-success text-decoration-none small fw-bold shadow-none p-0"
                onClick={handleResetPassword}
              >
                ¿Olvidaste tu contraseña?
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}