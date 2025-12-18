package ru.tgmaksim.gymnasium.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.view.LayoutInflater
import android.webkit.WebViewClient
import androidx.activity.addCallback
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.databinding.FragmentWebViewBinding

/**
 * Fragment для отображения веб-страницы в окне приложения
 * @author Максим Дрючин (tgmaksim)
 * @see newInstance
 * */
class WebViewFragment : Fragment() {
    private lateinit var ui: FragmentWebViewBinding
    private var url: String? = null

    companion object {
        private const val ARG_URL = "arg_url"

        /**
         * Создание экземпляра [WebViewFragment] для отображения веб-страницы
         * @param url ссылка на веб-страницу
         * @return экземпляр [WebViewFragment]
         * @author Максим Дрючин (tgmaksim)
         * */
        fun newInstance(url: String): WebViewFragment =
            WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, BuildConfig.DOCS_VIEW_ULR + url)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL)

        // Перехват кнопки "Назад" — если WebView может вернуться назад, он возвращается
        requireActivity().onBackPressedDispatcher.addCallback(this, true) {
            if (ui.webView.canGoBack()) {
                ui.webView.goBack()
            } else {
                // Возвращение назад и перерисовка
                parentFragmentManager.popBackStack()
                requireActivity().recreate()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentWebViewBinding.inflate(inflater, container, false)

        ui.webLoading.visibility = View.VISIBLE

        ui.webView.settings.apply {
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        ui.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Если страница так и не загрузилась
                if (view?.contentHeight == 0)
                    view.reload()
                else
                    ui.webLoading.visibility = View.GONE
            }
        }
        ui.webView.webChromeClient = WebChromeClient()

        url?.let { ui.webView.loadUrl(it) }

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("WebViewFragment(url=$url) загружена")
    }
}