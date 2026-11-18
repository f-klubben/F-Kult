import { expect, describe, it, vi, beforeEach, afterEach } from "vitest";
import { render, screen, fireEvent, waitFor, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import React from "react";
import LoginPage from "../pages/LoginPage";
import { ssoLoginUrl } from "../services/authService.jsx";
import * as matchers from "@testing-library/jest-dom/matchers"; 
expect.extend(matchers);

// --- ARRANGE helpers ---
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate, 
  };
});

globalThis.fetch = vi.fn();

// The backend session the page asks for when it mounts
const anonymousSession = () => ({ ok: true, json: async () => ({ authenticated: false }) });

function renderLoginPage(entry = "/login") {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <LoginPage />
    </MemoryRouter>
  );
}

describe("testing of LoginPage", () => {
  beforeEach(() => {
    sessionStorage.clear();
    fetch.mockReset();
    mockNavigate.mockReset();
    cleanup();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the F-Klub SSO buttons and the admin login", () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());

    renderLoginPage();

    // ASSERT
    expect(screen.getByText("Log ind på F-Kult")).toBeInTheDocument();
    expect(screen.getByTestId("sso-button-member")).toHaveTextContent("Continue as Fember");
    expect(screen.getByTestId("sso-button-volunteer")).toHaveTextContent("Continue as F-rivillig");
    expect(screen.getByTestId("show-admin-login")).toBeInTheDocument();
  });

  it("starts the F-Klub SSO login when an SSO button is clicked", () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());
    const location = { href: "" };
    vi.stubGlobal("location", location);

    renderLoginPage();

    // ACT
    fireEvent.click(screen.getByTestId("sso-button-member"));

    // ASSERT
    expect(location.href).toBe(ssoLoginUrl());
    expect(location.href).toBe("http://localhost:8080/oauth2/authorization/fklub");
  });

  it("starts the same F-Klub SSO login from the volunteer button", () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());
    const location = { href: "" };
    vi.stubGlobal("location", location);

    renderLoginPage();

    // ACT
    fireEvent.click(screen.getByTestId("sso-button-volunteer"));

    // ASSERT
    expect(location.href).toBe(ssoLoginUrl());
  });

  it("asks for a username and password when the admin login is opened", () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());

    renderLoginPage();

    // ACT
    fireEvent.click(screen.getByTestId("show-admin-login"));

    // ASSERT
    expect(screen.getByPlaceholderText("Admin brugernavn...")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Adgangskode...")).toBeInTheDocument();
    expect(screen.getByText("Log ind som admin")).toBeInTheDocument();
  });

  it("stores username and navigates on a successful admin login", async () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ authenticated: true, username: "root", admin: true }),
    });

    renderLoginPage();
    fireEvent.click(screen.getByTestId("show-admin-login"));

    // ACT
    fireEvent.change(screen.getByPlaceholderText("Admin brugernavn..."), { target: { value: "root" } });
    fireEvent.change(screen.getByPlaceholderText("Adgangskode..."), { target: { value: "admin" } });
    fireEvent.click(screen.getByText("Log ind som admin"));

    // ASSERT
    await waitFor(() => {
      expect(sessionStorage.getItem("username")).toBe("root");
      expect(mockNavigate).toHaveBeenCalledWith("/root");
    });
  });

  it("displays error on failed admin login", async () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());
    fetch.mockResolvedValueOnce({
      ok: false,
      text: async () => "Wrong username or password",
    });

    renderLoginPage();
    fireEvent.click(screen.getByTestId("show-admin-login"));

    // ACT
    fireEvent.change(screen.getByPlaceholderText("Admin brugernavn..."), { target: { value: "wrong" } });
    fireEvent.change(screen.getByPlaceholderText("Adgangskode..."), { target: { value: "wrong" } });
    fireEvent.click(screen.getByText("Log ind som admin"));

    // ASSERT
    await waitFor(() => {
      expect(screen.getByText("Wrong username or password")).toBeInTheDocument();
    });
  });

  it("navigates straight on when the session is already logged in", async () => {
    // ARRANGE
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ authenticated: true, username: "tester", admin: false }),
    });

    // ACT
    renderLoginPage();

    // ASSERT
    await waitFor(() => {
      expect(sessionStorage.getItem("username")).toBe("tester");
      expect(mockNavigate).toHaveBeenCalledWith("/tester");
    });
  });

  it("shows why the F-Klub SSO login failed", async () => {
    // ARRANGE
    fetch.mockResolvedValueOnce(anonymousSession());

    // ACT
    renderLoginPage("/login?auth=error&reason=user_banned");

    // ASSERT
    expect(await screen.findByText("Din bruger er udelukket fra F-Kult")).toBeInTheDocument();
  });
});
