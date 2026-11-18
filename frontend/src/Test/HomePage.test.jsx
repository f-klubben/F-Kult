import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { I18nextProvider } from "react-i18next";
import baseI18n from "../i18n";
import React from "react";
import Header from "../components/Header.jsx";
import HomePage from "../pages/HomePage.jsx";
import * as matchers from '@testing-library/jest-dom/matchers';
expect.extend(matchers);

// Unfinished components of homepage which are not used yet
vi.mock("../components/Theme/ThemeBrowser.jsx", () => ({
  default: () => <div data-testid="ThemeBrowser" />,
}));
vi.mock("../components/SoundSampleBrowser.jsx", () => ({
  default: () => <div data-testid="SoundSampleBrowser" />,
}));
vi.mock("../components/SubmitSSPage.jsx", () => ({
  default: () => <div data-testid="SubmitSSPage" />,
}));
vi.mock("../components/theme/NextTheme.jsx", () => ({
  default: () => <div data-testid="NextTheme" />,
}));

// mock navigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("HomePage Test + Header Test", () => {

  it("Test HomePage render", async () => {
    //arrange
    const i18n = baseI18n.cloneInstance();
    await i18n.changeLanguage("da");

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter initialEntries={["/spg"]}>
          <Routes>
            <Route
              path="/:username"
              element={
                <>
                  <Header />
                  <HomePage />
                </>
              }
            />
          </Routes>
        </MemoryRouter>
      </I18nextProvider>
    );
    
    await screen.findByRole("button", { name: "Temaer" });
    await screen.findByRole("button", { name: "Discord" });
    await screen.findByRole("button", { name: "Pizza" });

    

    expect(screen.getByText("Temaer")).toBeInTheDocument();
    expect(screen.getByText("Pizza")).toBeInTheDocument();
    expect(screen.getByText("Discord")).toBeInTheDocument();
    expect(screen.getByText("spg")).toBeInTheDocument();
    expect(screen.getByText("Info")).toBeInTheDocument();

  });


  it("starts in Danish, then switches to English after clicking header toggle", async () => {
    //arrange
    const i18n = baseI18n.cloneInstance();
    await i18n.changeLanguage("da");

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter initialEntries={["/spg"]}>
          <Routes>
            <Route
              path="/:username"
              element={
                <>
                  <Header />
                  <HomePage />
                </>
              }
            />
          </Routes>
        </MemoryRouter>
      </I18nextProvider>
    );

    //assert
    const dkBtn = await screen.findByRole("button", { name: "Temaer" });
    expect(dkBtn).toBeEnabled();
    expect(screen.getByText("Temaer")).toBeInTheDocument();
    expect(screen.queryByText("Themes")).not.toBeInTheDocument();

    //act
    const langToggle = screen.getByRole("button", { name: "Dansk" });
    fireEvent.click(langToggle);

    //assert
    const enBtn = await screen.findByRole("button", { name: "Themes" });
    expect(enBtn).toBeEnabled();
    expect(screen.queryByText("Temaer")).not.toBeInTheDocument();

    //act
    fireEvent.click(langToggle);
    //assert
    expect(screen.queryByText("Themes")).not.toBeInTheDocument();

  });

  it("It logs user out when logout button is pressed", async () => {
    sessionStorage.setItem("username", "spg");
    //arrange
    const i18n = baseI18n.cloneInstance();
    await i18n.changeLanguage("da");

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter initialEntries={["/spg"]}>
          <Routes>
            <Route
              path="/:username"
              element={
                <>
                  <Header />
                  <HomePage />
                </>
              }
            />
          </Routes>
        </MemoryRouter>
      </I18nextProvider>
    );


    const userButton = await screen.findByRole("button", { name: /user menu/i });
    fireEvent.click(userButton);
    expect(sessionStorage.getItem("username")).toBe("spg");

    const logoutBtn = await screen.findByRole("button", { name: "Log ud" });
    expect(logoutBtn).toBeInTheDocument();

    fireEvent.click(logoutBtn);
    expect(sessionStorage.getItem("username")).toBeNull();
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/login");
    });
  });
});
