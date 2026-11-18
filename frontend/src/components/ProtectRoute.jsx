import { Navigate, Outlet, useLocation, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { getSession } from "../services/authService.jsx";

export default function ProtectedRoute() {
  const location = useLocation();
  const { username } = useParams();
  // undefined = still asking the backend, "" = not logged in
  const [sessionUser, setSessionUser] = useState(undefined);

  // The backend session decides whether the user may pass, the stored username is only for the URL
  useEffect(() => {
    let cancelled = false;
    getSession().then(session => {
      if (cancelled) return;
      if (session.authenticated) sessionStorage.setItem("username", session.username);
      else sessionStorage.removeItem("username");
      setSessionUser(session.authenticated ? session.username : "");
    });
    return () => { cancelled = true; };
  }, []);

  if (sessionUser === undefined) return null;

  // If no session user, sent user to login page
  if (!sessionUser) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Session user exists but dosent match, change the user
  if (username && username !== sessionUser) {
    const parts = location.pathname.split("/");
    parts[parts.length - 1] = encodeURIComponent(sessionUser);
    return <Navigate to={parts.join("/")} replace />;
  }

  return <Outlet />; 
}
