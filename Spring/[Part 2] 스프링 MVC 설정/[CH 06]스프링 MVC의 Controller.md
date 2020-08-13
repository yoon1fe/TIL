## 스프링 MVC의 Controller

스프링 MVC를 사용하는 경우 작성되는 Controller는 다음과 같은 특징이 있습니다.

- HttpServletRequest, HttpServletResponse를 사용할 일이 거의 없이 필요한 기능 구현
- 다양한 타입의 파라미터 처리, 다양한 타입의 리턴 타입 사용 가능
- GET 방식, POST 방식 등 전송 방식에 대한 처리를 어노테이션으로 처리 가능
- 상속/인터페이스 방식 대신 어노테이션만으로도 필요한 설정 가능

### @Controller, @RequestMapping

SampleController 클래스를 만들어 봅시다.

```
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sample/*")
public class SampleController {

}
```

클래스 선언부에 @Controller라는 스프링 MVC에서 사용하는 어노테이션을 적용합니다. SampleController 클래스는 자동으로 스프링의 객체`(Bean)`로 등록되는데, 이유는 servlet-context.xml 에 있습니다.

servlet-context.xml 파일에서 `<context:component-scan>` 이라는 태그를 이용해서 지정된 패키지를 스캔하도록 되어 있습니다. 따라서 해당 패키지에 선언된 클래스들을 스캔하면서 스프링에서 객체 설정에 사용되는 어노테이션들을 가진 클래스들을 파악하고 필요하다면 이를 객체`(Bean)`로 생성해서 관리하게 됩니다.

스프링에서 관리되는 클래스라면 이클립스 화면상에서 작게 `S` 모양 아이콘이 뜹니다.



![img](https://blog.kakaocdn.net/dn/buLLbi/btqGzYIFEOc/U2waJofdmeJYKkOxHRMKk1/img.png)



클래스 선언부에는 @Controller와 함께 @RequestMapping을 많이 사용합니다.

@RequestMapping의 값은 현재 클래스의 모든 메소드들의 기본적인 URL 경로가 됩니다.

예를 들어, SampleController 클래스를 위와 같이 경로를 지정했다면, `~~/sample/aaa`, `~~/sample/bbb` 와 같은 URL들은 모두 SampleController에서 처리가 되는 것이죠.

@RequestMapping 어노테이션은 클래스의 선언과 메소드 선언에 사용할 수 있습니다.

```
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/sample/*")
@Log4j
public class SampleController {

    @RequestMapping("")
    public void basic() {
        log.info("basic................");
    }

}
```

WAS에서 실행해보면 스프링이 인식할 수 있는 정보가 출력되는 것을 볼 수 있습니다.



![img](https://blog.kakaocdn.net/dn/bgShFa/btqGAx4UakQ/D7t1G0u5L9zGLuSbbkXUQ1/img.png)



###  

### @RequestMapping의 변화

@Controller 어노테이션은 추가적인 속성을 지정할 수 없지만, @RequestMapping은 몇 가지 속성을 추가할 수 있습니다. 이 중 가장 많이 사용되는 속성은 method 속성입니다. method 속성은 흔히 GET 방식, POST 바식을 구분해서 사용할 때 이용합니다.

스프링 4.3버전부터는 이러한 @RequestMapping을 줄여서 사용할 수 있는 @GetMapping, @PostMapping 어노테이션이 등장합니다.

```
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/sample/*")
@Log4j
public class SampleController {

    @RequestMapping("")
    public void basic() {
        log.info("basic................");
    }

    @RequestMapping(value = "/basic", method = { RequestMethod.GET, RequestMethod.POST })
    public void basicGet() {
        log.info("basic get................");
    }

    @GetMapping("/basicOnlyGet")
    public void basicGet2() {
        log.info("basic get only get................");
    }

}
```

@RequestMapping은 GET, POST 방식을 모두 지원해야 하는 경우에 배열로 처리해서 지정할 수 있습니다. 일반적인 경우 GET, POST 방식만을 사용하지만, 최근에는 PUT, DELETE 방식 등도 많이 사용하는 추세입니다. @GetMapping의 경우 오직 GET 방식에만 사용할 수 있기 때문에, 간편하긴 하지만 기능에 대한 제한이 많은 편입니다.

###  

### Controller의 파라미터 수집

Controller를 작성할 때 가장 편리한 기능은 **파라미터가 자동으로 수집**되는 기능입니다.

이 기능을 이용하면 매번 request.getParameter()를 이용하지 않아도 됩니다.

SampleDTO 클래스를 만들어 봅시다.

```
import lombok.Data;

@Data
public class SampleDTO {
    private String name;
    private int age;
}
```

SampleDTO 클래스는 Lombok의 @Data 어노테이션을 이용해서 처리합니다.

@Data 어노테이션을 이용하면 getter/setter, equals(), toString() 등의 메소드를 자동으로 생성합니다.

SampleController의 메소드가 SampleDTO를 파라미터로 사용하게 되면 자동으로 setter 메소드가 동작하면서 파라미터를 수집합니다.

```
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yoon1fe.domain.SampleDTO;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/sample/*")
@Log4j
public class SampleController {

    @RequestMapping("")
    public void basic() {
        log.info("basic................");
    }

    @GetMapping("/ex01")
    public String ex01(SampleDTO dto) {
        log.info("" + dto);

        return "ex01";
    }
}
```

SampleController의 경로가 `/sample/*` 이므로 ex01() 메소드를 호출하는 경로는 `/sample/ex01` 이 됩니다. 이 메소드는 @GetMapping 어노테이션이 사용되었기 때문에 파라미터를 URL 뒤에 `?name=AAA&age=10` 과 같은 형태로 추가해서 호출할 수 있습니다.



![img](https://blog.kakaocdn.net/dn/dYXfyI/btqGAVEI5fA/wR1GyuYCM1O7xTjVI7fER1/img.png)

![img](https://blog.kakaocdn.net/dn/FPvfi/btqGAUZ5AMO/51qfeIAR2rqSkXSyqM0cek/img.png)



여기서 주목할 점은 자동으로 타입을 변환해서 처리한다는 점입니다.

####  

#### 파라미터의 수집과 변환

Controller가 파라미터를 수집하는 방식은 파라미터 타입에 따라 자동으로 변환하는 방식을 이용합니다.

만약 기본 자료형이나 문자열 등을 이용한다면 파라미터의 타입만 맞게 선언해주는 방식을 사용할 수 있습니다.

```
    @GetMapping("/ex02")
    public String ex02(@RequestParam("name") String name, @RequestParam("age") int age) {
        log.info("name: " + name);
        log.info("age: " + age);

        return "ex02";
    }
```



![img](https://blog.kakaocdn.net/dn/t0YIq/btqGC7jVLAu/GnmL5eVfu7Qf7iIEm5zIJK/img.png)



####  

#### 리스트, 배열 처리

동일한 이름의 파라미터가 여러 개 전달되는 경우에는 ArrayList<> 등을 이용해서 처리가 가능합니다.

```
    //리스트
    @GetMapping("/ex02List")
    public String ex02List(@RequestParam("ids")ArrayList<String> ids) {
        log.info("ids: " + ids);

        return "ex02List";
    }
    //배열
    @GetMapping("/ex02Array")
    public String ex02List(@RequestParam("ids")String[] ids) {
        log.info("Array ids: " + Arrays.toString(ids));

        return "ex02Array";
    }
```

스프링은 파라미터의 타입을 보고 객체를 생성하므로 파라미터의 타입은 `List<>`와 같이 인터페이스 타입이 아닌 실제적인 클래스 타입으로 지정됩니다. 위 코드의 경우 'ids'라는 이름의 파라미터가 여러개 전달돼도 `ArrayList<String>`이 생성되어 자동으로 수집됩니다.



![img](https://blog.kakaocdn.net/dn/bDVoEk/btqGAQpFLtq/JX4bCqbxl6mPr9zMT63AS1/img.png)



####  

#### 객체 리스트

전달하는 데이터가 SampleDTO와 같은 객체 타입이고 여러 개를 처리해야 한다면 약간의 작업을 통해서 한 번에 처리할 수 있습니다.

SampleDTOList 클래스를 만들어 줍시다.

```
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SampleDTOList {
    private List<SampleDTO> list;

    public SampleDTOList() {
        list = new ArrayList<>();
    }
}
```

그리고 SampleController에는 SampleDTOList 타입을 파라미터로 받는 메소드를 작성합니다.

```
    @GetMapping("/ex02Bean")
    public String ex02Bean(SampleDTOList list) {
        log.info("list dtos: " + list);

        return "ex02Bean";
    }
```

이 때 파라미터는 `[idx]`와 같은 형식으로 전달해서 처리할 수 있습니다.

참고로 톰캣은 버전에 따라서 위와 같은 문자열에서 대괄호를 특수문자로 허용하지 않을 수도 있습니다.

이는 `[`를 `%5B`로, `]`를 `%5D`로 변경해서 사용해 줍시다.

####  

#### @InitBinder

파라미터의 수집을 다른 용어로는 바인딩`(binding)`이라고 합니다. 변환이 가능한 데이터는 자동으로 변환되지만, 경우에 따라서는 파라미터를 변환해서 처리해야 하는 경우도 있습니다. 예를 들어, `2020-08-13` 과 같은 문자열을 `java.util.Date` 타입으로 변환하는 경우요.

스프링 Controller에서는 파라미터를 바인딩할 때 자동으로 호출되는 @InitBinder 어노테이션을 이용해서 이러한 변환을 처리합니다.

TodoDTO 클래스를 만들어 줍시다.

```
import java.util.Date;

import lombok.Data;

@Data
public class TodoDTO {
    private String title;
    private Date dueDate;
}
```

TodoDTO의 dueDate는 `java.util.Date` 타입입니다. `2020-08-13`과 같은 데이터를 변환하고자 할 때 문제가 발생하는데, 이를 @InitBinder를 통해 해결할 수 있습니다.

```
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(dateFormat, false));
    }

    @GetMapping("/ex03")
    public String ex03(TodoDTO todo) {
        log.info("todo: " + todo);
        return "ex03";
    }
```

####  

#### @DateTimeFormat

@InitBinder를 통해 날짜를 변환할 수도 있지만, 파라미터로 사용되는 인스턴스 변수에 @DateTimeFormat을 적용해서 간단히 변환할 수도 있습니다. @DateTimeFormat을 이용하면 @InitBinder를 사용하지 않아도 됩니다.

```
@Data
public class TodoDTO {
    private String title;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date dueDate;
}
```

###  

### Model 이라는 데이터 전달자

Controller의 메소드를 작성할 때는 특별히 Model이라는 타입을 파라미터로 지정할 수 있습니다. Model 객체는 JSP에 컨트롤러에서 생성된 데이터를 담아서 전달하는 역할을 하는 존재입니다. 이를 이용해 JSP와 같은 뷰`(view)`로 전달해야 하는 데이터를 담아서 보낼 수 있습니다. 메소드의 파라미터에 Model 타입이 지정된 경우에는 스프링은 특별히 Model 타입의 객체를 만들어서 메소드에 주입하게 됩니다.

Model은 모델 2 방식에서 사용하는 request.setAttribute()와 유사한 역할을 합니다.

```
// 서블릿에서 모델 2 방식으로 데이터를 전달하는 방식
request.setAttribute("serverTime", new java.util.Date());
RequestDispatcher dispatcher = requst.getRequestDispatcher("/WEB-INF/jsp/home.jsp");
dispatcher.forward(request, response);
// 스프링 MVC에서 Model을 이용해서 데이터를 전달하는 방식
public String home(Model model){
    model.addAttribute("serverTime", new java.util.Date());

    return "home";
}
```

메소드의 파라미터를 Model 타입으로 선언하면 자동으로 스프링 MVC에서 Model 타입의 객체를 만들어 주기 때문에 개발자의 입장에서는 필요한 데이터를 담아 주는 작업만 하면 됩니다. Model을 사용해야 하는 경우는 주로 Controller에 전달된 데이터를 이용해서 추가적인 데이터를 가져와야 하는 상황입니다.

e.g)

- 리스트 페이지 번호를 파라미터로 전달받고, 실제 데이터를 View로 전달해야 하는 경우
- 파라미터들에 대한 처리 후 결과를 전달해야 하는 경우

#### @ModelAttribute 어노테이션

웹 페이지의 구조는 Request에 전달된 데이터를 가지고, 필요하다면 추가적인 데이터를 생성해서 화면으로 전달하는 방식으로 동작합니다.

Model의 경우, 파라미터로 전달된 데이터는 존재하지 않지만 화면에서 필요한 데이터를 전달하기 위해 사용합니다. 예를 들어, 페이지 번호는 파라미터로 전달되지만, 결과 데이터를 전달하려면 Model에 담아서 전달합니다.

스프링 MVC의 Controller는 기본적으로 Java Beans 규칙에 맞는 객체는 다시 화면으로 객체를 전달합니다. 좁은 의미에서 Java Beans의 규칙은 단순히 생성자가 없거나 빈 생성자를 가져야 하고, getter/setter를 가진 클래스의 객체를 의미합니다. 앞에서 사용된 SampleDTO의 경우 Java Bean의 규칙에 부합하기 때문에 자동으로 다시 화면까지 전달됩니다. 전달될 때 클래스명의 앞글자는 소문자로 처리됩니다.

반면, 기본 자료형의 경우에는 파라미터로 선언하더라도 기본적으로 화면까지 전달되지는 않습니다.

SampleDTO 객체 타입과 int 타입을 파라미터로 가지는 ex04()를 만들어 줍시다.

```
    // SampleController.class
    @GetMapping("/ex04")
    public String ex04(SampleDTO dto, int page) {
        log.info("dto: " + dto);
        log.info("page: " + page);

        return "/sample/ex04";
    }
```

결과를 띄우기 위해서 ex04.jsp를 만들고 확인해봅시다.

```
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

<h2> SAMPLEDTO    ${sampleDTO }</h2>
<h2> PAGE        ${page }</h2>

</body>
</html>
```

서버를 실행하고 브라우저에서 `http://localhost:8080/sample/ex04?name=aaa&age=11&page=9` 을 호출해보면 화면에 SampleDTO만 전달된 것을 확인할 수 있습니다.



![img](https://blog.kakaocdn.net/dn/bSuSyB/btqGCVKIkVm/zEFvHFtoc45yskJbQRCEB0/img.png)



@ModelAttribute는 전달받은 파라미터를 강제로 Model에 담아서 전달하도록 할 때 사용하는 어노테이션입니다. @ModelAttribute가 걸린 파라미터는 타입에 관계없이 무조건 Model에 담아서 전달되기 때문에, 파라미터로 전달된 데이터를 다시 화면에서 사용해야 할 경우에 유용하게 사용됩니다.

기존에 int 타입의 데이터가 화면까지 전달되지 않는 걸 @ModelAttribute를 추가하면 화면까지 전달할 수 있습니다.

```
    @GetMapping("/ex04")
    public String ex04(SampleDTO dto, @ModelAttribute("page") int page) {
        log.info("dto: " + dto);
        log.info("page: " + page);

        return "/sample/ex04";
    }
```

기본 자료형에 @ModelAttribute를 적용할 때는 반드시 값을 지정해줍시다. (@ModelAttribute("page") 이렇게)



![img](https://blog.kakaocdn.net/dn/Mdbdd/btqGzX34xJl/ODGLvw47r9QQo0Lm9KAewK/img.png)



####  

#### RedirectAttributes

Model 타입과 더불어서 스프링 MVC가 자동으로 전달해 주는 타입 중에는 RedirectAttributes 타입이 존재합니다. RedirectAttributes는 조금 특별하게도 일회성으로 데이터를 전달하는 용도로 사용됩니다.

RedirectAttributes는 기존 서블릿에서`response.sendRedirect()`를 사용할 때와 동일한 용도로 사용됩니다.

```
// 서블릿에서 redirect 방식
response.sendRedirect("/home?name=aaa&age=10")
// 스프링 MVC를 이용하는 redirect 처리
rttr.addFlashAttribute("name", "AAA");
rttr.addFlashAttribute("age", 10);

return "redirect:/";
```

RedirectAttributes는 Model과 같이 파라미터로 선언해서 사용하고, `addFlashAttribute(이름, 값)` 메소드를 이용해서 화면에 한 번만 사용하고 다음에는 사용되지 않는 데이터를 전달하기 위해서 사용합니다.