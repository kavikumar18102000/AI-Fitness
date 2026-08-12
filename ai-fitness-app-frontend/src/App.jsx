import { Button } from "@mui/material"
import {BrowserRouter as Router, Navigate, Route, Routes, useLocation} from "react-router"
import { useContext, useEffect, useState } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { setCredentials } from "./store/authSlice";
import Box from '@mui/material/Box';
import ActivityForm from "./components/ActivityForm";
import ActivityList from "./components/ActivityList";

const ActivitiesPage = () => {
  <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
    <ActivityForm onActivitiesAdded = {()=> window.location.reload()} />
    <ActivityList />
  </Box>
}

function App() {
 const { token, tokenData, logIn, logOut, isAuthenticated } = useContext(AuthContext);
 const dispatch = useDispatch();
 const [auth, setAuthReady] = useState(false);

 useEffect(() => {
  if(token){
    dispatch(setCredentials({ token, user: tokenData }));
    setAuthReady(true);
  }
 }, [token, tokenData, dispatch]);

  return (
    <Router>
      {!token ? (
      <Button variant="contained" color="#dc004e" onClick={()=>{logIn();}}>LOGIN</Button>
      ) : (
         <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
          </Routes>
        </Box>
      )}
    </Router>
  )
}

export default App
