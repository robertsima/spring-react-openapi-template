import type {Meta, StoryObj} from '@storybook/react';
import {LoadingScreen} from './LoadingScreen';

const meta: Meta<typeof LoadingScreen> = {
    title: 'Screens/LoadingScreen',
    component: LoadingScreen,
    parameters: {backgrounds: {default: 'dark', values: [{name: 'dark', value: '#0a0a23'}]}},
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
export const CustomMessage: Story = {args: {message: 'Signing you in...'}};
