import React, { useEffect, useState } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
  ReferenceLine,
} from 'recharts';
import api from '../api/axios';

const NutritionChart = () => {
  const [data, setData] = useState([]);
  const [dailyGoal, setDailyGoal] = useState(2000); // Значення за замовчуванням

  useEffect(() => {
    api
      .get('/nutrition/logs')
      .then((response) => {
        const chartData = response.data.logs || [];
        const goal = response.data.dailyGoal || 2000;

        const sortedData = [...chartData].sort((a, b) => new Date(a.date) - new Date(b.date));

        setData(sortedData);
        setDailyGoal(goal);
      })
      .catch((error) => {
        console.error('Backend error loading nutrition analytics', error);
        setData([]);
      });
  }, []);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        width: '100%',
        paddingBottom: '50px',
      }}
    >
      {/* Заголовок використовує шрифт Alexandria */}
      <h2
        style={{
          fontSize: '32px',
          fontWeight: '700',
          letterSpacing: '-0.5px',
          marginTop: '60px',
          marginBottom: '40px',
          fontFamily: "'Alexandria', sans-serif",
          color: '#222',
        }}
      >
        Calorie Consumption Analytics
      </h2>

      <div
        style={{
          width: '95%',
          maxWidth: '900px',
          background: '#ffffff',
          padding: '30px',
          borderRadius: '20px',
          boxShadow: '0 15px 35px rgba(0,0,0,0.05)',
        }}
      >
        <ResponsiveContainer width="100%" aspect={1.7}>
          <LineChart data={data} margin={{ top: 10, right: 30, left: 10, bottom: 10 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#dfe6e9" />

            <XAxis
              dataKey="date"
              tick={{
                fill: '#555',
                fontSize: 14,
                fontFamily: "'Plus Jakarta Sans', sans-serif",
              }}
              axisLine={false}
              tickLine={false}
              dy={15}
            />

            <YAxis
              tick={{
                fill: '#555',
                fontSize: 14,
                fontFamily: "'Plus Jakarta Sans', sans-serif",
              }}
              axisLine={false}
              tickLine={false}
              dx={-10}
              domain={[0, 'dataMax + 500']}
            />

            <ReferenceLine
              y={dailyGoal}
              stroke="#ff7675"
              strokeDasharray="8 8"
              label={{ value: `goal: ${dailyGoal}`, position: 'insideTopRight', fill: '#ff7675' }}
            />

            <Tooltip
              contentStyle={{
                borderRadius: '12px',
                border: 'none',
                boxShadow: '0 10px 20px rgba(0,0,0,0.1)',
                fontSize: '16px',
                fontFamily: "'Plus Jakarta Sans', sans-serif",
              }}
            />

            {/* Легенда під графіком з вирівнюванням по лівому краю */}
            <Legend
              verticalAlign="bottom"
              align="left"
              height={50}
              iconType="circle"
              wrapperStyle={{
                fontSize: '16px',
                fontFamily: "'Plus Jakarta Sans', sans-serif",
                fontWeight: '600',
                color: '#555',
                paddingTop: '30px',
              }}
            />

            <Line
              name="Consumed Calories (kcal)"
              type="monotone"
              dataKey="consumedCalories"
              stroke="#667eea"
              strokeWidth={5}
              dot={{ r: 6, fill: '#667eea', strokeWidth: 3, stroke: '#fff' }}
              activeDot={{ r: 10 }}
              animationDuration={1500}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default NutritionChart;
