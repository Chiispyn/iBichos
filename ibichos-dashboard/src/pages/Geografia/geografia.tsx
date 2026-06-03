// src/pages/Geografia/Geografia.tsx
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, LabelList
} from 'recharts';
import { MapPin, Globe2, TrendingUp, Users } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useGeografia } from './useGeografia';

const COLORS = ['#3DDC84', '#4285F4', '#F4B400', '#DB4437', '#9C27B0', '#00BCD4', '#FF7043', '#5C6BC0', '#26A69A', '#EC407A'];

export default function Geografia() {
  const navigate = useNavigate();
  const {
    loading,
    regionData,
    comunaData,
    topComunasLimit, setTopComunasLimit,
    stats
  } = useGeografia();

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Analizando territorio...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="fw-bold text-success m-0">
          <Globe2 className="me-2 mb-1" />
          Distribución Territorial
        </h2>
        <button className="btn btn-outline-success btn-sm fw-bold" onClick={() => navigate('/analitica')}>
          Volver a Analíticas
        </button>
      </div>

      {/* Resumen Superior */}
      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card ibichos-card shadow-sm border-0 p-3">
            <div className="d-flex align-items-center">
              <div className="bg-success bg-opacity-10 p-3 rounded-3 me-3">
                <TrendingUp size={24} className="text-success" />
              </div>
              <div>
                <p className="text-muted small mb-0 fw-bold text-uppercase">Región Líder</p>
                <h4 className="fw-bold mb-0 text-dark">{stats.topRegion}</h4>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card ibichos-card shadow-sm border-0 p-3">
            <div className="d-flex align-items-center">
              <div className="bg-primary bg-opacity-10 p-3 rounded-3 me-3">
                <MapPin size={24} className="text-primary" />
              </div>
              <div>
                <p className="text-muted small mb-0 fw-bold text-uppercase">Comuna más Activa</p>
                <h4 className="fw-bold mb-0 text-dark">{stats.topComuna}</h4>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card ibichos-card shadow-sm border-0 p-3">
            <div className="d-flex align-items-center">
              <div className="bg-info bg-opacity-10 p-3 rounded-3 me-3">
                <Users size={24} className="text-info" />
              </div>
              <div>
                <p className="text-muted small mb-0 fw-bold text-uppercase">Comunas con Presencia</p>
                <h4 className="fw-bold mb-0 text-dark">{stats.totalLocs} <span className="fs-6 text-muted">zonas</span></h4>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-4">
        {/* Gráfico 1: Regiones (Todas) */}
        <div className="col-lg-12">
          <div className="card ibichos-card p-4 shadow-sm border-0">
            <h5 className="fw-bold mb-1 text-dark">Ranking Nacional por Regiones</h5>
            <p className="text-muted small mb-4">Distribución total de exploradores por región administrativa</p>

            <div style={{ height: regionData.length * 40 + 50, minHeight: '400px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={regionData} layout="vertical" margin={{ top: 10, right: 50, left: 20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.1} />
                  <XAxis type="number" hide />
                  <YAxis
                    dataKey="name"
                    type="category"
                    width={180}
                    tick={{ fontSize: 12, fill: '#475569', fontWeight: 600 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip
                    cursor={{ fill: '#F1F5F9' }}
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                  />
                  <Bar dataKey="value" radius={[0, 6, 6, 0]} barSize={25}>
                    {regionData.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                    <LabelList dataKey="value" position="right" fill="#334155" fontSize={13} fontWeight="bold" />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Gráfico 2: Comunas (Top 20) */}
        <div className="col-lg-12">
          <div className="card ibichos-card p-4 shadow-sm border-0 mt-2">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <div>
                <h5 className="fw-bold mb-1 text-dark">Ranking de Comunas</h5>
                <p className="text-muted small mb-0">Concentración local de la comunidad iBichos</p>
              </div>
              <div className="btn-group shadow-sm">
                <button
                  className={`btn btn-sm fw-bold px-3 ${topComunasLimit === 5 ? 'btn-primary' : 'btn-outline-secondary bg-white'}`}
                  onClick={() => setTopComunasLimit(5)}
                >
                  Top 5
                </button>
                <button
                  className={`btn btn-sm fw-bold px-3 ${topComunasLimit === 10 ? 'btn-primary' : 'btn-outline-secondary bg-white'}`}
                  onClick={() => setTopComunasLimit(10)}
                >
                  Top 10
                </button>
              </div>
            </div>

            <div style={{ height: topComunasLimit === 5 ? 220 : 380, transition: 'height 0.3s ease' }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={comunaData.slice(0, topComunasLimit)} layout="vertical" margin={{ top: 10, right: 50, left: 20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.1} />
                  <XAxis type="number" hide />
                  <YAxis
                    dataKey="name"
                    type="category"
                    width={150}
                    tick={{ fontSize: 12, fill: '#475569', fontWeight: 600 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip
                    cursor={{ fill: '#F1F5F9' }}
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                  />
                  <Bar dataKey="value" fill="#4285F4" radius={[0, 6, 6, 0]} barSize={20}>
                    <LabelList dataKey="value" position="right" fill="#334155" fontSize={12} fontWeight="bold" />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}