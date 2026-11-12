import {useEffect} from "react";
import {useNavigate} from "react-router";

export const NavigateTo = (props: { to: string }) => {
    const navigate = useNavigate();
    useEffect(() => {
        navigate(props.to);
    }, [navigate, props.to])
    return <></>
}
