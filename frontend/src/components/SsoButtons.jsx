import flogo from "../assets/flogo.svg";
import React from "react";
import { ssoLoginUrl } from "../services/authService.jsx";

export default function SsoButtons() {
    const startSso = () => { window.location.href = ssoLoginUrl(); };

    return (
        <div className="fclub-sso-stack">
            <button
                type="button"
                data-testid="sso-button-member"
                className="fclub-sso-button member"
                onClick={startSso}
            >
                <img className="fclub-sso-logo" src={flogo} alt=""/>
                <span>Continue as Fember</span>
            </button>

            <button
                type="button"
                data-testid="sso-button-volunteer"
                className="fclub-sso-button volunteer"
                onClick={startSso}
            >
                <img className="fclub-sso-logo" src={flogo} alt=""/>
                <span>Continue as F-rivillig</span>
            </button>
        </div>
    );
}
