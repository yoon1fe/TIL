## Chrome 92 버전 업에 따른 이슈..

- iframe 사용하는 경우 alert(), confirm(), prompt() 함수 호출 불가..
- 레이어 팝업으로 변경해야 한다!!
- 근데 컨트롤러 단에서 예외 처리에서 lucy의 AlertAndGoView 클래스를 사용해서 alert() 알림을 보냈다..



AlertAndGoView 클래스는 NavigationView 란 추상 클래스를 상속받아 구현되어 있는데, 얘는 스프링 프레임워크의 View 클래스를 구현하고 있다.. 그래서 NavigationView 를 상속받은 LayerPopupView 클래스 생성하는 쪽으로..
