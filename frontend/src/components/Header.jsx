import logoPNG from '../assets/logo.png';
import pizzaWEBP from '../assets/pizza.webp'
import discordWEBP from '../assets/discord.webp'
import userPNG from "../assets/User.png"
import englishPNG from "../assets/english.png"
import danishPNG from "../assets/danish.png"
import React, {useEffect} from "react";
import {useNavigate, useParams} from "react-router-dom";
import { useState, useRef } from "react";
import { useTranslation } from "react-i18next";
import {getSession, logout as endSession} from "../services/authService.jsx";

export default function Header(){
    // Consts
    const navigate = useNavigate();
    const {username} = useParams();
    const [open, setOpen] = useState(false);
    const menuRef = useRef(null);
    const [adminBool, setAdminBool] = useState(false)

    const {t, i18n} = useTranslation();

    const [lang, setLang] = useState(() => localStorage.getItem("lang") || "da"); // Get the last used language, default to danish

    // When rendered, load the language last used
    useEffect(() => {
        i18n.changeLanguage(lang);
        localStorage.setItem("lang", lang);
        document.documentElement.setAttribute("lang", lang);
        window.dispatchEvent(new CustomEvent("lang-change", { detail: lang }));

    }, [lang, i18n]);

    // Swap language
    const toggleLang = () => {
        setLang(prev => (prev === "da" ? "en" : "da"));
        //test log
        //console.log(lang);
    };

    // Check if the user is admin when loaded, used for dropdown menu
    useEffect(() => {
        getSession().then(session => setAdminBool(session.authenticated && session.admin))
    }, [username]);

    // Function to log out: remove all evidence and end the session on the backend.
    const logout = async () =>{
        sessionStorage.removeItem("username");
        await endSession();
        navigate("/login");
    };


    // Close dropdown if clicked outside
    useEffect(() => {
        function handleClickOutside(e) {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                setOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <header className="relative sm:pb-5 flex justify-between top-0 inset-x-0">
            {/* Home button */}
            <NavButton
                icon={logoPNG}
                onClick={() => {navigate(`/${username}`)}}
            />
            {/* Title */}
            <h1 className="absolute left-1/2 -translate-x-1/2 -translate-y-2.5 sm:-translate-y-0 top-2 text-center text-2xl sm:text-3xl font-serif hover:cursor-pointer" onClick={() => navigate(`/${username}`)}>F-Kult</h1>
            <div className="flex">
                {/* Language button */}
                <NavButton
                label={lang === "da" ? "Dansk" : "English"}
                icon={lang === "da" ? danishPNG : englishPNG}
                onClick={toggleLang}
                />

                <div className="hidden sm:flex">
                    {/* Pizza button */}
                    <NavButton
                        label={t("Pizza")}
                        icon={pizzaWEBP}
                        onClick={() => {window.open("https://f-kult-pizza-bestiller.vercel.app/", "_blank")}}
                    />

                    {/* Discord button */}
                    <NavButton
                        label={t("Discord")}
                        icon={discordWEBP}
                        onClick={() => {window.open("https://discord.gg/zV3GEZyY6z", "_blank")}}
                    />
                </div>
                {/* User button */}
                <div className="relative flex-col sm:ml-1" ref={menuRef}>
                     <button
                        className="flex bg-primary drop-shadow-lg transition-colors hover:bg-btn-hover-secondary cursor-pointer rounded-4xl border size-7 sm:size-12 items-center justify-center"
                        onClick={()=> setOpen(prev => !prev)}
                        title={username}
                        >
                        <img className="w-5 sm:w-9 h-5 sm:h-9" src={userPNG} alt="User menu"/>
                     </button>
                     <p className="text-center align-top text-xs sm:text-sm">{username}</p>
                    {/* Dropdown menu */}
                     {open && (
                        <div className="absolute right-0 sm:top-14 w-21 sm:w-36 bg-primary border rounded-xl shadow-lg z-50">
                            <button
                                className={"cursor-pointer block w-full text-left px-4 py-2 hover:bg-btn-hover-secondary rounded-t-xl " + (adminBool ? "" : "hidden")}
                                onClick={()=>{
                                    setOpen(false);
                                    navigate(`/admin/${username}`);
                                }}
                                >
                                {t("Admin")}
                            </button>
                            <button
                                className="cursor-pointer block w-full text-left px-4 py-2 hover:bg-btn-hover-secondary text-text-error rounded-b-xl"
                                onClick={logout}
                                >
                                {t("logout")}
                            </button>
                        </div>
                     )}
                </div>
            </div>
        </header>
    )
}

// Standardized navigation buttons
function NavButton({ icon, label, onClick }) {
    // Home button
    if (icon === logoPNG){
        return (
            <button className="flex -translate-y-3 sm:-translate-y-4 transition-colors hover:bg-btn-hover-secondary cursor-pointer rounded-4xl size-12 sm:size-20 items-center justify-center" onClick={onClick} title={label}>
                <img src={icon} alt={label}/>
            </button>

        );
    }

    // Other buttons
    return (
        <div className="flex flex-col items-center mx-1">
            <button className="flex bg-primary drop-shadow-lg transition-colors hover:bg-btn-hover-secondary cursor-pointer rounded-4xl border size-7 sm:size-12 items-center justify-center" onClick={onClick} title={label}>
                <img className="w-5 sm:w-9 h-5 sm:h-9" src={icon} alt={label}/>
            </button>
            <p className="text-center align-top text-xs sm:text-sm">{label}</p>
        </div>

    );
}