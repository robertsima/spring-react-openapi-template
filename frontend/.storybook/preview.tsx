import type { Preview } from "@storybook/react";
import { ThemeProvider } from "@mui/material/styles";
import { defaultTheme } from "../src/themes/theme1";

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={defaultTheme}>
        <Story />
      </ThemeProvider>
    ),
  ],
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
  },
};

export default preview;
