import {Rectangle} from "../models/rectangle";

export function intersects(firstRect: Rectangle, secondRect: Rectangle) {
    return !(firstRect.left > secondRect.right ||
        firstRect.right < secondRect.left ||
        firstRect.bottom < secondRect.top ||
        firstRect.top > secondRect.bottom)
}

export function computeDistance(firstRect: Rectangle, secondRect: Rectangle) {
    const firstRectCenter = {
        x: firstRect.left + (firstRect.right - firstRect.left) / 2,
        y: firstRect.top + (firstRect.bottom - firstRect.top) / 2
    };
    const secondRectCenter = {
        x: secondRect.left + (secondRect.right - secondRect.left) / 2,
        y: secondRect.top + (secondRect.bottom - secondRect.top) / 2
    };

    const distanceX = Math.abs(firstRectCenter.x - secondRectCenter.x);
    const distanceY = Math.abs(firstRectCenter.y - secondRectCenter.y);

    return Math.sqrt(distanceX * distanceX + distanceY * distanceY);

}

export function computeRelativePositionToParent(absolutePosition: { x: number, y: number },
                                                absolutePositionParent: { x: number, y: number }) {
    return {
        x: (absolutePosition.x - absolutePositionParent.x),
        y: (absolutePosition.y - absolutePositionParent.y)
    }
}
