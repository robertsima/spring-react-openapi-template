import {expect, test} from '@playwright/test';

// Placeholder smoke test: app boots, Windows SSO (or localdev) signs the user in, the generated client works.
test('home page signs in and searches users', async ({page}) => {
    await page.goto('/');
    await expect(page.getByText(/Signed in as/)).toBeVisible();

    await page.getByLabel('User name').fill('Alice Johnson');
    await page.getByRole('button', {name: 'Search'}).click();
    await expect(page.getByText('Alice Johnson')).toBeVisible();
});
