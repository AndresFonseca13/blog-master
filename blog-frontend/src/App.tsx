import {Routes, Route, BrowserRouter} from 'react-router-dom';
import {  Home, Login, Register, Profile, Settings} from './pages';
import { ProtectedRoute } from './components/ProtectedRoute.tsx';
import { AuthProvider} from "./context/AuthContext.tsx";

const App = () =>  {
  return (
      <AuthProvider>
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
      </AuthProvider>
  )
}

export default App
