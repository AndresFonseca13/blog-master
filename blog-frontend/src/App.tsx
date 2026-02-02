import {Routes, Route, BrowserRouter} from 'react-router-dom';
import {  Home, Login, Register, Profile, Settings} from './pages';
import { ProtectedRoute } from './components/ProtectedRoute.tsx';

const App = () =>  {
  return (
      <BrowserRouter>
      <Routes>
          <Route path="/" element={<Home/>}/>
          <Route path="/login" element={<Login/>}/>
          <Route path="/register" element={<Register/>}/>
          <Route path="/settings" element={
              <ProtectedRoute>
                  <Settings/>
              </ProtectedRoute>
          }/>
         <Route path="/profile" element={
             <ProtectedRoute>
                 <Profile/>
            </ProtectedRoute>
         }/>
         </Routes>
      </BrowserRouter>
  )
}

export default App
