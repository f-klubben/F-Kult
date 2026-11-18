import logo from "../assets/logo.png";
import Topholt from "../assets/Topholt.png";
import React from "react"; //allows testing to progress
import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { adminLogin, getSession } from '../services/authService.jsx'
import SsoButtons from '../components/SsoButtons.jsx'

// Reasons the backend can report after a failed F-Klub SSO login
const SSO_ERRORS = {
    access_denied: "Login med F-Klub SSO blev afbrudt",
    user_banned: "Din bruger er udelukket fra F-Kult",
    unknown_user: "Kunne ikke oprette din F-Kult bruger",
    missing_username: "F-Klub SSO kunne ikke fortælle hvem du er",
};

export default function LoginPage() {
    const [showTopholt, setShowTopholt] = useState(false); //easter egg feature
    //admin login handling
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [showAdminLogin, setShowAdminLogin] = useState(false);
    const [error, setError] = useState("");
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const authResult = searchParams.get("auth");
    const authReason = searchParams.get("reason");

    //easter egg to show picture of Topholt when logo is clicked
    useEffect(() => {
        const onEsc = (e) => e.key === "Escape" && setShowTopholt(false);
        window.addEventListener("keydown", onEsc);
        return () => window.removeEventListener("keydown", onEsc);
    }, []);

    //navigate user to front page if the backend session says they are logged in,
    //this is also the way back from the F-Klub SSO login
    useEffect(() => {
        if (authResult === "error") {
            setError(SSO_ERRORS[authReason] || "Login med F-Klub SSO mislykkedes");
        }

        getSession().then(session => {
            if (!session.authenticated) return;
            sessionStorage.setItem("username", session.username);
            navigate(`/${session.username}`);
        });
    }, [navigate, authResult, authReason]);

    //submit admin credentials handler
    const handleAdminSubmit = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const session = await adminLogin(username, password);
            sessionStorage.setItem("username", session.username);
            navigate(`/${session.username}`);
        } catch (e) {
            setError(e.message || "Unknown error");
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center -m-5 p-5 bg-topholt">
            <div className="absolute -translate-y-3 sm:-translate-y-4 top-6 left-6 size-12 sm:size-20" onClick={() => setShowTopholt(true)} aria-label="Open Topholt">
                <img
                    src={logo}
                    alt="F-Klub Logo"
                />
            </div>

            <div className="bg-primary drop-shadow-xl rounded-2xl px-5 sm:px-10 py-4 sm:py-8 w-full max-w-md flex flex-col gap-4.5">
                <h1 className="text-lg sm:text-2xl font-serif font-bold text-center text-text-secondary">Log ind på F-Kult</h1>

                {/* F-Klub SSO is the normal login */}
                <SsoButtons />
                <p className="text-center text-xs sm:text-sm text-text-secondary -mt-2.5">Brug din stregbruger og F-kode</p>

                {error && <p className="text-text-error text-xs sm:text-sm text-center mt-1">{error}</p>}

                {/* The admin login is for when F-Klub SSO is not an option */}
                {!showAdminLogin ? (
                    <button
                        type="button"
                        data-testid="show-admin-login"
                        className="text-xs sm:text-sm text-text-secondary hover:text-text-primary underline cursor-pointer"
                        onClick={() => setShowAdminLogin(true)}
                    >
                        Admin log ind
                    </button>
                ) : (
                    <form className="flex flex-col gap-3 border-t pt-4" onSubmit={handleAdminSubmit}>
                        <input
                            type="text"
                            placeholder="Admin brugernavn..."
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="border bg-primary rounded-2xl px-4 py-2.5 text-text-secondary"
                        />
                        <input
                            type="password"
                            placeholder="Adgangskode..."
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="border bg-primary rounded-2xl px-4 py-2.5 text-text-secondary"
                        />
                        <button type="submit" className="btn-primary w-full">
                            Log ind som admin
                        </button>
                    </form>
                )}
            </div>

            {showTopholt && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-text-primary/50"
                    onClick={() => setShowTopholt(false)}
                    role="dialog"
                    aria-modal="true"
                >
                    <div className="bg-primary rounded-xl shadow-xl p-2 max-w-xl w-auto" onClick={(e) => e.stopPropagation()}>
                        <img 
                            src={Topholt} 
                            alt="Topholt" 
                            className="max-h-[90vw] w-auto mx-auto rounded-lg object-contain"
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
