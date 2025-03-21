var _ctx = $("body").attr('data-ctx');


// 设置当前 URL 对应的菜单项的 active 样式
function setActiveMenu() {
    var currentPath = window.location.pathname;

    // 遍历所有菜单项
    $('#side-menu li a').each(function() {
        // 获取菜单项的 href 属性
        var href = $(this).attr('href');

        // 判断 href 是否包含当前路径
        if (currentPath.indexOf(href) !== -1) {
            // 为匹配的菜单项添加 active 类
            var $li = $(this).closest('li');
            $li.addClass('active');

            // 向上查找父级菜单，直到找到 #side-menu 或最上级的 <li>，并为每个父级菜单项添加 active 类
            $li.parents('li').each(function() {
                var $parentLi = $(this);

                // 如果找到了 #side-menu，则停止查找
                if ($parentLi.closest('#side-menu').length > 0) {
                    // 为父级菜单添加 active 类
                    $parentLi.addClass('active');

                    // 如果父级菜单有子菜单，展开子菜单（添加 'in' 类）
                    $parentLi.children('ul').addClass('in');
                }
            });

            // 如果菜单项有子菜单，展开子菜单
            $(this).closest('li').children('ul').addClass('in');
        } else {
            // 移除没有匹配的菜单项的 active 类
            $(this).closest('li').removeClass('active');

            // 关闭子菜单
            $(this).closest('li').children('ul').removeClass('in');
        }
    });
}

$(document).ready(function () {
    //加载左侧导航
    $("#leftnav").load(_ctx + '/leftnav', function (response, status, xhr) {
        if (status === 'success') {
            //加载完成执行导航组件
            $('#side-menu').metisMenu();
            // 调用函数设置当前 URL 对应的菜单项的 active 样式
            setActiveMenu();
            if ($("body").hasClass('fixed-sidebar')) {
                $('.sidebar-scroll').slimScroll({
                    height: '100%',
                    railVisible: false,
                    color: "#65cea7",
                    opacity: .8,
                    size: '4px',
                    borderRadius: '0',
                    railBorderRadius: '0',
                    distance: 0
                });
            }
        }
    });
    //加载顶部导航
    $("#topnav").load(_ctx + '/topnav', function (response, status, xhr) {
        if (status === 'success') {
            //加载完成执行按钮点击事件
            $('.navbar-minimalize').click(function () {
                $("body").toggleClass("mini-navbar");
                SmoothlyMenu();
            });
        }
    });

    // Add body-small class if window less than 768px
    if ($(this).width() < 769) {
        $('body').addClass('body-small')
    } else {
        $('body').removeClass('body-small')
    }
    // Collapse ibox function
    $('.collapse-link').click(function () {
        var ibox = $(this).closest('div.ibox');
        var button = $(this).find('i');
        var content = ibox.find('div.ibox-content');
        content.slideToggle(200);
        button.toggleClass('fa-chevron-up').toggleClass('fa-chevron-down');
        ibox.toggleClass('').toggleClass('border-bottom');
        setTimeout(function () {
            ibox.resize();
            ibox.find('[id^=map-]').resize();
        }, 50);
    });

    // Close ibox function
    $('.close-link').click(function () {
        var content = $(this).closest('div.ibox');
        content.remove();
    });

    // Close menu in canvas mode
    $('.close-canvas-menu').click(function () {
        $("body").toggleClass("mini-navbar");
        SmoothlyMenu();
    });

    // Open close right sidebar
    $('.right-sidebar-toggle').click(function () {
        $('#right-sidebar').toggleClass('sidebar-open');
    });

    // Initialize slimscroll for right sidebar
    $('.sidebar-container').slimScroll({
        height: '100%',
        railOpacity: 0.4,
        wheelStep: 10
    });

    // Open close small chat
    $('.open-small-chat').click(function () {
        $(this).children().toggleClass('fa-comments').toggleClass('fa-remove');
        $('.small-chat-box').toggleClass('active');
    });

    // Initialize slimscroll for small chat
    $('.small-chat-box .content').slimScroll({
        height: '234px',
        railOpacity: 0.4
    });

    $('.check-link').click(function () {
        var button = $(this).find('i');
        var label = $(this).next('span');
        button.toggleClass('fa-check-square').toggleClass('fa-square-o');
        label.toggleClass('todo-completed');
        return false;
    });

    // Tooltips demo
    $('.tooltip-demo').tooltip({
        selector: "[data-toggle=tooltip]",
        container: "body"
    });

    // Move modal to body
    // Fix Bootstrap backdrop issu with animation.css
    $('.modal').appendTo("body");

    // Full height of sidebar
    function fix_height() {
        var heightWithoutNavbar = $("body > #wrapper");
        $(".sidebard-panel").css("min-height", heightWithoutNavbar + "px");

        var navbarHeigh = $('nav.navbar-default').height() + 61;
        var wrapperHeigh = $('#page-wrapper').height() + 61;

        if (navbarHeigh > wrapperHeigh) {
            $('#page-wrapper').css("min-height", navbarHeigh + "px");
        }

        if (navbarHeigh < wrapperHeigh) {
            $('#page-wrapper').css("min-height", $(window).height() + "px");
        }

        if ($('body').hasClass('fixed-nav')) {
            $('#page-wrapper').css("min-height", $(window).height() + "px");
        }

    }

    fix_height();

    // Move right sidebar top after scroll
    $(window).scroll(function () {
        if ($(window).scrollTop() > 0 && !$('body').hasClass('fixed-nav')) {
            $('#right-sidebar').addClass('sidebar-top');
        } else {
            $('#right-sidebar').removeClass('sidebar-top');
        }
    });

    $(window).bind("load resize scroll", function () {
        if (!$("body").hasClass('body-small')) {
            fix_height();
        }
    });

    $("[data-toggle=popover]").popover();

    // Add slimscroll to element
    $('.full-height-scroll').slimscroll({
        height: '100%'
    });

    if (localStorageSupport) {

        var collapse = localStorage.getItem("collapse_menu");
        var fixedsidebar = localStorage.getItem("fixedsidebar");
        var fixednavbar = localStorage.getItem("fixednavbar");
        var boxedlayout = localStorage.getItem("boxedlayout");
        var fixedfooter = localStorage.getItem("fixedfooter");

        var body = $('body');

        if (fixedsidebar === 'on') {
            body.addClass('fixed-sidebar');
            $('.sidebar-collapse').slimScroll({
                height: '100%',
                railOpacity: 0.9
            });
        }

        if (collapse === 'on') {
            if (body.hasClass('fixed-sidebar')) {
                if (!body.hasClass('body-small')) {
                    body.addClass('mini-navbar');
                }
            } else {
                if (!body.hasClass('body-small')) {
                    body.addClass('mini-navbar');
                }

            }
        }

        if (fixednavbar === 'on') {
            $(".navbar-static-top").removeClass('navbar-static-top').addClass('navbar-fixed-top');
            body.addClass('fixed-nav');
        }

        if (boxedlayout === 'on') {
            body.addClass('boxed-layout');
        }

        if (fixedfooter === 'on') {
            $(".footer").addClass('fixed');
        }
    }
});


// Minimalize menu when screen is less than 768px
$(window).bind("resize", function () {
    if ($(this).width() < 769) {
        $('body').addClass('body-small')
    } else {
        $('body').removeClass('body-small')
    }
});


// check if browser support HTML5 local storage
function localStorageSupport() {
    return (('localStorage' in window) && window['localStorage'] !== null)
}

function SmoothlyMenu() {
    if (!$('body').hasClass('mini-navbar') || $('body').hasClass('body-small')) {
        // Hide menu in order to smoothly turn on when maximize menu
        $('#side-menu', '#side-head').hide();
        //$().hide();
        // For smoothly turn on menu
        setTimeout(
            function () {
                $('#side-menu', '#side-head').fadeIn(200);
            }, 200);
    } else if ($('body').hasClass('fixed-sidebar')) {
        $('#side-menu', '#side-head').hide();
        setTimeout(
            function () {
                $('#side-menu').fadeIn(400)

            }, 100);
    } else {
        // Remove all inline style from jquery fadeIn function to reset menu state
        $('#side-menu').removeAttr('style');
    }
};

