import {AfterViewInit, Component, ElementRef, HostListener, ViewChild} from "@angular/core";

@Component({
    selector: 'ks-unauthorized',
    template: `        
        <div class="wrapper" fxLayout="column" fxLayoutGap="45px" fxLayoutAlign="center center" fxFill>
            <svg xmlns="http://www.w3.org/2000/svg" id="robot-error" viewBox="0 0 260 118.9">
                <defs>
                    <clipPath id="white-clip">
                        <circle id="white-eye" fill="#cacaca" cx="130" cy="65" r="20"/>
                    </clipPath>
                    <text id="text-s" class="error-text" y="106"> 403</text>
                </defs>

                <use xlink:href="#text-s" x="-0.5px" y="-1px" fill="#20354e"></use>
                <use xlink:href="#text-s" fill="#365880"></use>
                <g id="robot">
                    <g id="eye-wrap">
                        <use xlink:href="#white-eye"></use>
                        <circle #eyef id="eyef" class="eye" clip-path="url(#white-clip)" fill="#000" stroke="#2aa7cc"
                                stroke-width="2" stroke-miterlimit="10" cx="130" cy="65" r="11"/>
                        <ellipse id="white-eye" fill="#365880" cx="130" cy="40" rx="18" ry="12"/>
                    </g>
                    <circle class="lightblue" cx="105" cy="32" r="2.5" id="tornillo"/>
                    <use xlink:href="#tornillo" x="50"></use>
                    <use xlink:href="#tornillo" x="50" y="60"></use>
                    <use xlink:href="#tornillo" y="60"></use>
                </g>
            </svg>
            <h1 translate>GENERAL.UNAUTHORIZED</h1>
            <h2 translate>GENERAL.CONTACT_ADMIN</h2>
        </div>
    `,
    styleUrls: ['./unauthorized.component.scss']
})
export class UnauthorizedComponent implements AfterViewInit {

    root = document.documentElement;
    @ViewChild('eyef', { static: true }) eyef: ElementRef;
    cx: string;
    cy: string;


    @HostListener('mousemove', ['$event'])
    mouseMove(evt: MouseEvent) {
        let x = evt.clientX / innerWidth;
        let y = evt.clientY / innerHeight;

        this.root.style.setProperty("--mouse-x", x.toString());
        this.root.style.setProperty("--mouse-y", y.toString());

        this.cx = (115 + 30 * x).toString();
        this.cy = (50 + 30 * y).toString();
        this.eyef.nativeElement.setAttribute("cx", this.cx);
        this.eyef.nativeElement.setAttribute("cy", this.cy);
    }

    ngAfterViewInit(): void {
        this.cx = this.eyef.nativeElement.getAttribute("cx");
        this.cy = this.eyef.nativeElement.getAttribute("cy");
        this.root.style.setProperty("--mouse-x", '0.5');
        this.root.style.setProperty("--mouse-y", '0.5');
    }
}
