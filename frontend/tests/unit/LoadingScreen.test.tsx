import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {LoadingScreen} from '../../src/screens/LoadingScreen/LoadingScreen';

describe('LoadingScreen', () => {
    it('shows the message', () => {
        render(<LoadingScreen message="Signing you in..."/>);
        expect(screen.getByText('Signing you in...')).toBeInTheDocument();
    });
});
